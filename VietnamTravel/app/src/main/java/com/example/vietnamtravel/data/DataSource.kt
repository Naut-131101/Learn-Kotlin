package com.example.vietnamtravel.data

import com.example.vietnamtravel.R
import com.example.vietnamtravel.model.Place
import com.example.vietnamtravel.model.Region

object DataSource {
    val places: List<Place> = listOf(
        Place(
            id = 1,
            name = "Vịnh Hạ Long",
            province = "Quảng Ninh",
            region = Region.NORTH,
            imageRes = R.drawable.ha_long,
            rating = 4.9,
            shortDescription = "Di sản thiên nhiên thế giới với gần 2.000 đảo đá vôi nhô lên giữa làn nước xanh ngọc."
        ),
        Place(
            id = 2,
            name = "Sa Pa",
            province = "Lào Cai",
            region = Region.NORTH,
            imageRes = R.drawable.sa_pa,
            rating = 4.7,
            shortDescription = "Thị trấn sương mù với ruộng bậc thang, bản làng dân tộc và đỉnh Fansipan."
        ),
        Place(
            id = 3,
            name = "Tràng An",
            province = "Ninh Bình",
            region = Region.NORTH,
            imageRes = R.drawable.trang_an,
            rating = 4.8,
            shortDescription = "Ngồi thuyền xuyên qua hang động và thung lũng núi đá vôi giữa đồng lúa."
        ),
        Place(
            id = 4,
            name = "Cố đô Huế",
            province = "Huế",
            region = Region.CENTRAL,
            imageRes = R.drawable.hue,
            rating = 4.6,
            shortDescription = "Kinh thành, lăng tẩm triều Nguyễn bên dòng sông Hương thơ mộng."
        ),
        Place(
            id = 5,
            name = "Phố cổ Hội An",
            province = "Đà Nẵng",
            region = Region.CENTRAL,
            imageRes = R.drawable.hoi_an,
            rating = 4.9,
            shortDescription = "Những dãy nhà vàng, đèn lồng rực rỡ và chùa Cầu hơn 400 năm tuổi."
        ),
        Place(
            id = 6,
            name = "Phong Nha - Kẻ Bàng",
            province = "Quảng Trị",
            region = Region.CENTRAL,
            imageRes = R.drawable.phong_nha,
            rating = 4.8,
            shortDescription = "Vương quốc hang động với sông ngầm, thạch nhũ và rừng nguyên sinh."
        ),
        Place(
            id = 7,
            name = "Vịnh Nha Trang",
            province = "Khánh Hòa",
            region = Region.CENTRAL,
            imageRes = R.drawable.nha_trang,
            rating = 4.5,
            shortDescription = "Bãi biển dài, nước trong và các đảo lặn ngắm san hô."
        ),
        Place(
            id = 8,
            name = "Đảo Phú Quốc",
            province = "An Giang",
            region = Region.SOUTH,
            imageRes = R.drawable.phu_quoc,
            rating = 4.7,
            shortDescription = "Đảo ngọc với biển xanh, hoàng hôn rực rỡ và làng chài yên bình."
        ),
        Place(
            id = 9,
            name = "Chợ nổi Cái Răng",
            province = "Cần Thơ",
            region = Region.SOUTH,
            imageRes = R.drawable.cai_rang,
            rating = 4.4,
            shortDescription = "Chợ họp trên sông từ tờ mờ sáng, nét văn hoá đặc trưng miền Tây."
        ),
        Place(
            id = 10,
            name = "Địa đạo Củ Chi",
            province = "TP. Hồ Chí Minh",
            region = Region.SOUTH,
            imageRes = R.drawable.cu_chi,
            rating = 4.5,
            shortDescription = "Hệ thống đường hầm dài hàng trăm km, chứng tích lịch sử kháng chiến."
        ),
    )
}