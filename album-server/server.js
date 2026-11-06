import express from 'express';
import { v2 as cloudinary } from 'cloudinary';

// SDK tự đọc biến môi trường CLOUDINARY_URL=cloudinary://<api_key>:<api_secret>@<cloud_name>
cloudinary.config({ secure: true });
const { cloud_name: cloudName, api_key: apiKey, api_secret: apiSecret } = cloudinary.config();
if (!cloudName || !apiKey || !apiSecret) {
  console.error('Thiếu CLOUDINARY_URL trong file .env');
  process.exit(1);
}

const PORT = process.env.PORT ?? 3000;
const FOLDER = 'android-album'; // thư mục chứa ảnh trên Cloudinary
const TAG = 'android-album';    // gắn tag để liệt kê ảnh của album
const API_BASE = cloudinary.config().upload_prefix ?? 'https://api.cloudinary.com';

const app = express();
app.use(express.json());

/** Thông tin ảnh gửi về app: chỉ những gì app cần, kèm URL thumbnail đã biến đổi. */
function toPhoto(resource) {
  return {
    id: resource.public_id,
    url: resource.secure_url,
    // Chèn transformation vào URL: cắt vuông 400x400, tự chọn định dạng và chất lượng
    thumbUrl: resource.secure_url.replace('/upload/', '/upload/c_fill,g_auto,w_400,h_400,f_auto,q_auto/'),
    width: resource.width,
    height: resource.height,
    bytes: resource.bytes,
    createdAt: resource.created_at,
  };
}

// 1. Ký upload: app gửi ảnh thẳng lên Cloudinary, server chỉ "đóng dấu" các tham số.
app.post('/api/uploads/signature', (req, res) => {
  const timestamp = Math.round(Date.now() / 1000);
  const params = { timestamp, folder: FOLDER, tags: TAG };
  const signature = cloudinary.utils.api_sign_request(params, apiSecret);
  res.json({
    uploadUrl: `${API_BASE}/v1_1/${cloudName}/image/upload`,
    apiKey,
    timestamp,
    folder: FOLDER,
    tags: TAG,
    signature,
  });
});

// 2. Danh sách ảnh của album, mới nhất trước.
app.get('/api/photos', async (req, res) => {
  const result = await cloudinary.api.resources_by_tag(TAG, { max_results: 100 });
  const photos = result.resources
    .map(toPhoto)
    .sort((a, b) => b.createdAt.localeCompare(a.createdAt));
  res.json(photos);
});

// 3. Xoá ảnh. Chỉ cho xoá ảnh nằm trong thư mục của album.
app.delete('/api/photos', async (req, res) => {
  const id = String(req.query.id ?? '');
  if (!id.startsWith(`${FOLDER}/`)) {
    return res.status(400).json({ error: 'id không hợp lệ' });
  }
  const result = await cloudinary.uploader.destroy(id, { invalidate: true });
  res.json({ deleted: result.result === 'ok' });
});

// Lỗi trong route async (Express 5 tự chuyển tới đây) -> trả JSON thay vì trang HTML
app.use((err, req, res, next) => {
  console.error(err);
  res.status(500).json({ error: err.message ?? 'Lỗi máy chủ' });
});

app.listen(PORT, '0.0.0.0', () => {
  console.log(`Album server: http://localhost:${PORT}  (cloud: ${cloudName})`);
});