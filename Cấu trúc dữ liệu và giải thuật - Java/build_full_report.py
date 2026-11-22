import os
import shutil
import zipfile
import xml.etree.ElementTree as ET
from docx_generators import (
    qn, create_element, sub_element,
    make_page_break, make_heading_1, make_heading_2, make_heading_3,
    make_para, make_rich_para, make_bullet, make_callout, make_code_box, make_table
)

def build_report():
    print("=== ĐANG BẮT ĐẦU TẠO BÁO CÁO WORD CHI TIẾT BÀI TẬP LỚN ===")
    
    # 1. Parse original document.xml to extract cover page
    tree = ET.parse('docx_extracted/word/document.xml')
    root = tree.getroot()
    ns = {'w': 'http://schemas.openxmlformats.org/wordprocessingml/2006/main'}
    body = root.find('w:body', ns)
    
    # Collect cover elements: elements 0 to 22 (the paragraphs of cover page)
    # Element 23 is the original sectPr with page borders
    cover_elements = []
    orig_sectPr = None
    
    for el in list(body):
        tag = el.tag.split('}')[-1]
        if tag == 'sectPr':
            orig_sectPr = el
            body.remove(el)
        else:
            cover_elements.append(el)
            body.remove(el)
            
    print(f"Preserved {len(cover_elements)} cover page elements.")
    
    # Put cover elements back into body
    for el in cover_elements:
        body.append(el)
        
    # The last paragraph of the cover page (paragraph 22) should contain the Section 1 sectPr
    # with w:type val="nextPage" so that Section 2 starts on the next page cleanly!
    last_cover_p = cover_elements[-1]
    pPr = last_cover_p.find(qn('pPr'))
    if pPr is None:
        pPr = ET.Element(qn('pPr'))
        last_cover_p.insert(0, pPr)
        
    # Add type="nextPage" to orig_sectPr
    type_el = sub_element(orig_sectPr, 'type', val='nextPage')
    pPr.append(orig_sectPr)
    
    # Helper to add elements directly to body
    def add(el):
        body.append(el)
        
    # =========================================================================
    # LỜI CẢM ƠN & LỜI CAM ĐOAN
    # =========================================================================
    add(make_heading_1("LỜI CẢM ƠN"))
    add(make_para("Lời đầu tiên, nhóm sinh viên chúng em xin gửi lời cảm ơn chân thành và sâu sắc nhất tới Ban Giám hiệu Học viện Công nghệ Bưu chính Viễn thông, các thầy cô giáo trong Khoa Công nghệ Thông tin đã tạo điều kiện học tập và truyền đạt những kiến thức quý báu cho chúng em trong suốt quá trình theo học môn Cấu trúc dữ liệu và giải thuật."))
    add(make_para("Đặc biệt, nhóm chúng em xin bày tỏ lòng biết ơn sâu sắc tới ThS. Vũ Đình Tân – Giảng viên trực tiếp hướng dẫn và giảng dạy bộ môn Cấu trúc dữ liệu và giải thuật - Java. Trong suốt thời gian thực hiện bài tập lớn, thầy đã luôn tận tình định hướng phương pháp tư duy, chỉ dẫn những cấu trúc thuật toán quan trọng và giải đáp những khúc mắc kỹ thuật để nhóm có thể hoàn thiện đề tài một cách chỉn chu, đúng đắn và chuẩn mực nhất."))
    add(make_para("Mặc dù nhóm chúng em đã nỗ lực hết mình để tìm hiểu, thiết kế và hiện thực hóa đề tài, song do giới hạn về mặt thời gian và kinh nghiệm thực tiễn nên khó tránh khỏi những thiếu sót nhất định. Nhóm rất mong nhận được những lời nhận xét, góp ý quý báu từ thầy để đề tài ngày càng được hoàn thiện hơn."))
    
    add(make_heading_1("LỜI CAM ĐOAN"))
    add(make_para("Nhóm sinh viên chúng em xin cam đoan:"))
    add(make_bullet("Tính trung thực", "Báo cáo bài tập lớn với đề tài 'Phân tích mạng xã hội bằng đồ thị' là công trình nghiên cứu và cài đặt thực tế do chính các thành viên trong Nhóm 12 thực hiện dưới sự định hướng của giảng viên hướng dẫn ThS. Vũ Đình Tân."))
    add(make_bullet("Không sao chép", "Toàn bộ mã nguồn chương trình, sơ đồ kiến trúc, dữ liệu thử nghiệm và các nội dung trình bày trong báo cáo đều được xây dựng độc lập, không sao chép từ bất kỳ bài tập lớn nào khác."))
    add(make_bullet("Trách nhiệm", "Các số liệu thực nghiệm và kết quả thuật toán được trích xuất hoàn toàn trung thực từ hệ thống thực thi do nhóm tự lập trình trên nền tảng ngôn ngữ Java SE."))
    
    add(make_rich_para([
        ("Hà Nội, ngày 15 tháng 10 năm 2026\n", False, True, "000000"),
        ("Đại diện nhóm thực hiện (Trưởng nhóm)\n", True, False, "000000"),
        ("Nguyễn Minh Tiến", True, False, "1F497D")
    ], align="right", indent=False))
    
    add(make_page_break())
    
    # =========================================================================
    # BẢNG PHÂN CÔNG NHIỆM VỤ THEO ĐỀ BÀI
    # =========================================================================
    add(make_heading_1("BẢNG PHÂN CÔNG CÔNG VIỆC TRONG NHÓM"))
    add(make_para("Căn cứ theo yêu cầu phân chia công việc trong đề bài: Người 1 phụ trách xây dựng class User và cấu trúc đồ thị; Người 2 phụ trách viết các hàm xử lý phân tích dữ liệu mạng xã hội, nhóm 12 đã phân công nhiệm vụ cụ thể và phối hợp thực hiện như sau:"))
    
    headers_assign = ["STT", "Thành viên", "Mã sinh viên", "Vai trò", "Nhiệm vụ được phân công", "Kết quả / Đóng góp"]
    rows_assign = [
        [
            "1",
            "Nguyễn Minh Tiến",
            "B22DVCN308",
            "Trưởng nhóm\n(Người 1)",
            "- Xây dựng lớp đối tượng User (mô hình hóa Node trong đồ thị mạng xã hội: ID, họ tên, tuổi, chuyên ngành, quê quán, sở thích, danh sách bạn bè).\n"
            "- Thiết kế và cài đặt cấu trúc Đồ thị vô hướng (SocialGraph) sử dụng danh sách kề với HashMap<String, Set<String>> và HashMap<String, List<String>>.\n"
            "- Hiện thực hóa các phương thức quản lý đỉnh (addUser, removeUser) và cạnh quan hệ bạn bè hai chiều (addFriendship, removeFriendship, isFriend).\n"
            "- Xây dựng bộ nạp/xuất dữ liệu đồ thị từ tệp tin (File I/O).\n"
            "- Thiết kế kiến trúc điều khiển và giao diện dòng lệnh tương tác trực quan (SocialNetworkApp, Main).",
            "Hoàn thành xuất sắc\n(100% nhiệm vụ)"
        ],
        [
            "2",
            "Đồng Duy Phúc",
            "B22DVCN152",
            "Thành viên\n(Người 2)",
            "- Nghiên cứu, thiết kế và cài đặt lớp xử lý phân tích mạng xã hội (SocialAnalyzer).\n"
            "- Cài đặt Chức năng 1: Xem bạn bè chung giữa 2 người dùng (Mutual Friends) dựa trên phép giao hai tập hợp kề.\n"
            "- Cài đặt Chức năng 2: Gợi ý kết bạn mới bằng thuật toán BFS (Breadth-First Search) theo nguyên lý Friends of Friends và xếp hạng ứng viên.\n"
            "- Cài đặt Chức năng 2: Gợi ý kết bạn mới bằng thuật toán DFS (Depth-First Search) khám phá các nhánh kết nối sâu.\n"
            "- Cài đặt Chức năng 3: Tìm đường kết nối ngắn nhất giữa 2 người bằng BFS (Degrees of Separation) kèm truy vết lộ trình.\n"
            "- Cài đặt các phân tích mở rộng: Đếm thành phần liên thông (Connected Components), Top Influencers, độ tương đồng Jaccard.\n"
            "- Xây dựng bộ kiểm thử tự động (SocialNetworkTest).",
            "Hoàn thành xuất sắc\n(100% nhiệm vụ)"
        ]
    ]
    add(make_table(headers_assign, rows_assign, col_widths=[500, 1500, 1200, 1100, 3900, 1300]))
    
    add(make_page_break())
    
    # =========================================================================
    # DANH MỤC THUẬT NGỮ & MỤC LỤC
    # =========================================================================
    add(make_heading_1("DANH MỤC CÁC THUẬT NGỮ VÀ VIẾT TẮT"))
    headers_terms = ["Thuật ngữ / Viết tắt", "Tên tiếng Anh đầy đủ", "Ý nghĩa trong đề tài"]
    rows_terms = [
        ["BFS", "Breadth-First Search", "Thuật toán duyệt đồ thị theo chiều rộng (theo từng tầng/khoảng cách)"],
        ["DFS", "Depth-First Search", "Thuật toán duyệt đồ thị theo chiều sâu (đi sâu theo từng nhánh)"],
        ["Graph", "Undirected Graph", "Cấu trúc dữ liệu đồ thị vô hướng mô hình hóa mạng xã hội"],
        ["Vertex / Node", "Vertex / Node", "Đỉnh của đồ thị, đại diện cho một người dùng (User)"],
        ["Edge", "Undirected Edge", "Cạnh vô hướng, đại diện cho mối quan hệ bạn bè hai chiều"],
        ["Adjacency List", "Adjacency List", "Danh sách kề, lưu trữ tập các bạn bè kết nối trực tiếp với mỗi người dùng"],
        ["Degrees of Separation", "Degrees of Separation", "Số bước chuyển tiếp (bậc phân cách) trên đường kết nối ngắn nhất giữa 2 người"],
        ["Mutual Friends", "Mutual / Common Friends", "Những người bạn chung cùng quen biết cả 2 người dùng"],
        ["Degree Centrality", "Degree Centrality", "Độ đo tầm ảnh hưởng dựa trên số lượng bạn bè trực tiếp của người dùng"],
        ["Jaccard Similarity", "Jaccard Similarity Coefficient", "Chỉ số tương đồng đánh giá mức độ trùng lặp tập bạn bè giữa 2 người"]
    ]
    add(make_table(headers_terms, rows_terms, col_widths=[1800, 2900, 4800]))
    
    add(make_heading_1("MỤC LỤC TỔNG THỂ"))
    toc_items = [
        ("LỜI CẢM ƠN", "Trang 2"),
        ("LỜI CAM ĐOAN", "Trang 2"),
        ("BẢNG PHÂN CÔNG CÔNG VIỆC TRONG NHÓM", "Trang 3"),
        ("DANH MỤC CÁC THUẬT NGỮ VÀ VIẾT TẮT", "Trang 4"),
        ("CHƯƠNG 1: TỔNG QUAN ĐỀ TÀI VÀ PHÂN TÍCH YÊU CẦU", "Trang 5"),
        ("   1.1. Đặt vấn đề và tính cấp thiết của đề tài", "Trang 5"),
        ("   1.2. Mục tiêu nghiên cứu và phạm vi ứng dụng", "Trang 5"),
        ("   1.3. Đặc tả yêu cầu chức năng và phi chức năng", "Trang 6"),
        ("   1.4. Ý nghĩa thực tiễn của đề tài", "Trang 7"),
        ("CHƯƠNG 2: CƠ SỞ LÝ THUYẾT & CẤU TRÚC DỮ LIỆU ĐỒ THỊ", "Trang 8"),
        ("   2.1. Lý thuyết đồ thị trong mô hình hóa mạng xã hội", "Trang 8"),
        ("   2.2. Lựa chọn cấu trúc dữ liệu biểu diễn đồ thị", "Trang 9"),
        ("   2.3. Giải thuật Duyệt theo chiều rộng (Breadth-First Search - BFS)", "Trang 10"),
        ("   2.4. Giải thuật Duyệt theo chiều sâu (Depth-First Search - DFS)", "Trang 11"),
        ("   2.5. Các bài toán phân tích mạng xã hội đặc trưng", "Trang 12"),
        ("   2.6. Phân tích độ phức tạp thời gian và không gian", "Trang 13"),
        ("CHƯƠNG 3: THIẾT KẾ VÀ CÀI ĐẶT HỆ THỐNG", "Trang 14"),
        ("   3.1. Kiến trúc tổng thể và tổ chức gói phần mềm (Package)", "Trang 14"),
        ("   3.2. Thiết kế chi tiết các lớp đối tượng", "Trang 15"),
        ("   3.3. Cài đặt các giải thuật cốt lõi và giải thích mã nguồn", "Trang 18"),
        ("CHƯƠNG 4: KẾT QUẢ THỰC NGHIỆM VÀ ĐÁNH GIÁ", "Trang 22"),
        ("   4.1. Kịch bản thực nghiệm và tập dữ liệu sinh viên PTIT", "Trang 22"),
        ("   4.2. Kết quả chạy thực tế của các chức năng", "Trang 23"),
        ("   4.3. Đánh giá kiểm thử tự động toàn diện", "Trang 27"),
        ("CHƯƠNG 5: KẾT LUẬN VÀ HƯỚNG PHÁT TRIỂN", "Trang 28"),
        ("   5.1. Kết quả đạt được so với yêu cầu đề bài", "Trang 28"),
        ("   5.2. Đánh giá ưu điểm và hạn chế", "Trang 28"),
        ("   5.3. Hướng phát triển trong tương lai", "Trang 29"),
        ("TÀI LIỆU THAM KHẢO", "Trang 30")
    ]
    for title, pg in toc_items:
        add(make_rich_para([
            (title, title.startswith("CHƯƠNG") or title.startswith("LỜI") or title.startswith("BẢNG") or title.startswith("DANH") or title.startswith("TÀI"), False, "003366" if title.startswith("CHƯƠNG") else "000000"),
            (" ............................................................................................................................ "[:90 - len(title)], False, False, "BDC3C7"),
            (pg, True, False, "1F497D")
        ], align="both", indent=False, space_after='30'))
        
    add(make_page_break())
    
    # =========================================================================
    # CHƯƠNG 1
    # =========================================================================
    add(make_heading_1("CHƯƠNG 1: TỔNG QUAN ĐỀ TÀI VÀ PHÂN TÍCH YÊU CẦU"))
    
    add(make_heading_2("1.1. Đặt vấn đề và tính cấp thiết của đề tài"))
    add(make_para("Trong kỷ nguyên công nghệ số và sự bùng nổ của cuộc Cách mạng Công nghiệp lần thứ tư, mạng xã hội (Social Network) đã trở thành một phần không thể thiếu trong đời sống hàng ngày của hàng tỷ người trên toàn cầu. Các nền tảng mạng xã hội như Facebook, Instagram, LinkedIn, Zalo không chỉ đơn thuần là nơi chia sẻ thông tin cá nhân mà còn là những hệ sinh thái kết nối phức tạp, nơi mỗi hành vi kết bạn, tương tác hay gợi ý nội dung đều dựa trên các mô hình phân tích dữ liệu mạng lưới quy mô cực lớn."))
    add(make_para("Về bản chất toán học và khoa học máy tính, cấu trúc mạng xã hội được mô hình hóa tự nhiên và trực quan nhất dưới dạng một Đồ thị (Graph). Trong đó, mỗi cá nhân hoặc tài khoản người dùng được biểu diễn bởi một Đỉnh (Vertex/Node), và mỗi mối quan hệ kết bạn giữa hai người dùng được biểu diễn bởi một Cạnh (Edge). Việc áp dụng các cấu trúc dữ liệu đồ thị và thuật toán duyệt đồ thị (Graph Traversal) như BFS, DFS cho phép hệ thống giải quyết nhanh chóng các bài toán cốt lõi: tìm kiếm bạn bè chung, phát hiện những người quen tiềm năng (Friends of Friends), và xác định lộ trình kết nối ngắn nhất giữa hai cá nhân bất kỳ trong xã hội."))
    add(make_para("Xuất phát từ tầm quan trọng thực tiễn và tính học thuật cao của vấn đề, nhóm 12 lựa chọn thực hiện đề tài: 'Phân tích mạng xã hội bằng đồ thị (Graph traversal)' nhằm vận dụng nhuần nhuyễn các kiến thức về cấu trúc dữ liệu đồ thị vô hướng và thuật toán tìm kiếm vào một bài toán thực tế sinh động."))
    
    add(make_heading_2("1.2. Mục tiêu nghiên cứu và phạm vi ứng dụng"))
    add(make_para("Mục tiêu trọng tâm của đề tài bao gồm:"))
    add(make_bullet("Về mặt cấu trúc dữ liệu", "Thiết kế và cài đặt cấu trúc Đồ thị vô hướng (Undirected Graph) tối ưu để biểu diễn các thực thể người dùng (User) và quan hệ bạn bè hai chiều bằng danh sách kề (Adjacency List) với HashMap<String, Set<String>>."))
    add(make_bullet("Về mặt thuật toán", "Ứng dụng thành công hai thuật toán duyệt đồ thị kinh điển là Duyệt theo chiều rộng (BFS - Breadth-First Search) và Duyệt theo chiều sâu (DFS - Depth-First Search) để giải quyết trọn vẹn 3 bài toán trọng tâm:"))
    add(make_bullet("  + Bài toán 1", "Xem và phân tích danh sách bạn bè chung giữa hai người dùng bất kỳ."))
    add(make_bullet("  + Bài toán 2", "Gợi ý bạn mới (Friend Recommendation) dựa trên nguyên lý bạn của bạn (BFS) và duyệt sâu mạng lưới (DFS), kết hợp chấm điểm độ tương đồng."))
    add(make_bullet("  + Bài toán 3", "Tìm đường kết nối ngắn nhất giữa 2 người (Shortest Connection Path / Degrees of Separation) và truy vết chính xác lộ trình."))
    add(make_bullet("Về mặt mở rộng ứng dụng", "Phân tích các thành phần liên thông (Connected Components) để nhận diện các nhóm cộng đồng độc lập, tính toán tầm ảnh hưởng của các thành viên (Degree Centrality), và đo lường độ tương đồng Jaccard."))
    
    add(make_heading_2("1.3. Đặc tả yêu cầu chức năng và phi chức năng"))
    add(make_heading_3("1.3.1. Các yêu cầu chức năng (Functional Requirements - FR)"))
    add(make_para("Hệ thống phải đáp ứng đầy đủ các yêu cầu chức năng sau đây:"))
    add(make_bullet("FR-01: Quản lý người dùng", "Cho phép thêm mới người dùng, xóa người dùng, tra cứu thông tin chi tiết người dùng (ID, họ tên, tuổi, chuyên ngành, quê quán, sở thích)."))
    add(make_bullet("FR-02: Quản lý quan hệ bạn bè", "Thiết lập liên kết kết bạn hai chiều (cạnh vô hướng) giữa 2 người; hủy liên kết kết bạn; kiểm tra trạng thái bạn bè."))
    add(make_bullet("FR-03: Xem bạn bè chung (Mutual Friends)", "Tìm kiếm và hiển thị danh sách các người bạn chung cùng quen biết cả 2 người dùng được chỉ định."))
    add(make_bullet("FR-04: Gợi ý bạn mới bằng BFS", "Duyệt theo chiều rộng ở tầng khoảng cách 2 (bạn của bạn nhưng chưa kết bạn với mình), sắp xếp danh sách gợi ý theo số lượng bạn chung giảm dần và điểm tương đồng."))
    add(make_bullet("FR-05: Gợi ý bạn mới bằng DFS", "Khám phá các nhánh kết nối sâu (độ sâu giới hạn k) để tìm kiếm những người có liên kết gián tiếp trong mạng lưới."))
    add(make_bullet("FR-06: Tìm đường kết nối ngắn nhất (BFS Shortest Path)", "Tìm lộ trình ngắn nhất nối giữa 2 người dùng bất kỳ, xuất ra chuỗi liên kết và số bước chuyển tiếp (Degrees of Separation). Xử lý an toàn khi 2 người thuộc 2 nhóm rời rạc."))
    add(make_bullet("FR-07: Phân tích mạng mở rộng", "Thống kê Top người dùng có nhiều bạn bè nhất (Top Influencers); phân tách đồ thị thành các nhóm độc lập (Connected Components)."))
    add(make_bullet("FR-08: Lưu trữ và nạp dữ liệu (File I/O)", "Hỗ trợ xuất đồ thị ra file văn bản và đọc dữ liệu đồ thị từ file, khôi phục dữ liệu mẫu."))
    
    add(make_heading_3("1.3.2. Các yêu cầu phi chức năng (Non-Functional Requirements - NFR)"))
    add(make_bullet("NFR-01: Tính đúng đắn (Correctness)", "Các thuật toán BFS và DFS phải đảm bảo tính toán chính xác, không rơi vào vòng lặp vô tận, xử lý triệt để các trường hợp biên."))
    add(make_bullet("NFR-02: Hiệu năng tối ưu (Efficiency)", "Sử dụng cấu trúc HashMap và HashSet với chi phí thời gian tra cứu O(1); thuật toán BFS/DFS có độ phức tạp thời gian O(V + E) đáp ứng tức thời."))
    add(make_bullet("NFR-03: Thiết kế hướng đối tượng (OOP & Clean Code)", "Mã nguồn tuân thủ chặt chẽ các nguyên lý lập trình hướng đối tượng trong Java (Encapsulation, Polymorphism, Single Responsibility), phân tách các package rõ ràng."))
    add(make_bullet("NFR-04: Giao diện trực quan (Usability)", "Giao diện Console tương tác Menu dễ sử dụng, hỗ trợ đầy đủ tiếng Việt Unicode, thông báo phản hồi thân thiện."))
    
    add(make_page_break())
    
    # =========================================================================
    # CHƯƠNG 2
    # =========================================================================
    add(make_heading_1("CHƯƠNG 2: CƠ SỞ LÝ THUYẾT & CẤU TRÚC DỮ LIỆU ĐỒ THỊ"))
    
    add(make_heading_2("2.1. Lý thuyết đồ thị trong mô hình hóa mạng xã hội"))
    add(make_para("Trong toán học rời rạc và cấu trúc dữ liệu, đồ thị là một cấu trúc trừu tượng bao gồm tập hợp các đỉnh và tập hợp các cạnh nối các đỉnh đó."))
    add(make_para("Định nghĩa toán học: Một đồ thị vô hướng G được xác định bởi bộ đôi G = (V, E), trong đó:"))
    add(make_bullet("Tập đỉnh V (Vertices)", "Tập hợp hữu hạn các thực thể người dùng trong mạng xã hội: V = {u_1, u_2, ..., u_n}. Mỗi đỉnh chứa các thuộc tính thực thể như mã định danh, họ tên, thuộc tính nhân khẩu học."))
    add(make_bullet("Tập cạnh E (Edges)", "Tập hợp các cặp đỉnh không có thứ tự E ⊆ {(u, v) | u, v ∈ V, u ≠ v}. Vì là mối quan hệ bạn bè hai chiều đối xứng (khi u là bạn của v thì đồng thời v cũng là bạn của u), ta có quan hệ vô hướng: (u, v) ∈ E ⟺ (v, u) ∈ E."))
    add(make_bullet("Bậc của đỉnh deg(u)", "Số lượng cạnh liên thuộc với đỉnh u, biểu thị số lượng bạn bè trực tiếp của người dùng u trong mạng xã hội."))
    
    add(make_callout("Đặc thù của Đồ thị Mạng xã hội", 
                     "Đồ thị mạng xã hội trong thực tế là một 'Đồ thị thưa' (Sparse Graph). Mặc dù số lượng đỉnh V có thể rất lớn, nhưng số lượng kết nối trực tiếp của mỗi cá nhân thường chỉ dao động từ vài chục đến vài trăm bạn bè (|E| << |V|^2). Do đó, việc lựa chọn cấu trúc dữ liệu lưu trữ là yếu tố sống còn quyết định hiệu năng của toàn bộ hệ thống."))
                     
    add(make_heading_2("2.2. Lựa chọn cấu trúc dữ liệu biểu diễn đồ thị"))
    add(make_para("Có hai phương pháp kinh điển để biểu diễn đồ thị trong máy tính:"))
    
    headers_repr = ["Tiêu chí so sánh", "Ma trận kề (Adjacency Matrix)", "Danh sách kề (Adjacency List) - Lựa chọn của nhóm"]
    rows_repr = [
        ["Cấu trúc lưu trữ", "Mảng hai chiều kích thước V x V boolean", "Bảng băm HashMap<String, Set<String>>"],
        ["Không gian bộ nhớ", "Tốn kém O(V^2), lãng phí lớn khi đồ thị thưa", "Tối ưu vượt trội: O(V + E)"],
        ["Kiểm tra bạn bè u và v", "O(1) thông qua matrix[u][v]", "O(1) trung bình thông qua HashSet.contains()"],
        ["Duyệt tất cả bạn bè của u", "O(V) (buộc phải quét toàn bộ dòng)", "O(deg(u)) (chỉ duyệt đúng danh sách hàng xóm)"],
        ["Thêm / Xóa đỉnh", "Tốn kém O(V^2) do phải cấp phát lại mảng", "O(1) linh hoạt với HashMap.put() và remove()"],
        ["Mức độ phù hợp", "Chỉ phù hợp với đồ thị dày (Dense Graph)", "Tối ưu tuyệt đối cho mạng xã hội"]
    ]
    add(make_table(headers_repr, rows_repr, col_widths=[1800, 3600, 4100]))
    
    add(make_para("Quyết định thiết kế của nhóm: Theo gợi ý đề bài, nhóm cài đặt cấu trúc đồ thị dựa trên bảng băm HashMap kết hợp Set/List:"))
    add(make_bullet("userMap", "Lưu trữ ánh xạ Map<String, User> từ ID sang đối tượng User đầy đủ."))
    add(make_bullet("adjacencyList", "Lưu trữ ánh xạ Map<String, Set<String>> từ ID người dùng sang tập hợp các ID bạn bè trực tiếp. Việc sử dụng LinkedHashSet thay cho List thuần túy giúp ngăn chặn tự động các cạnh trùng lặp và cho phép kiểm tra tồn tại quan hệ bạn bè trong thời gian O(1)."))
    
    add(make_heading_2("2.3. Giải thuật Duyệt theo chiều rộng (Breadth-First Search - BFS)"))
    add(make_para("Giải thuật Duyệt theo chiều rộng (BFS) là một trong những giải thuật nền tảng quan trọng nhất trong lý thuyết đồ thị. BFS bắt đầu từ một đỉnh nguồn s và tiến hành duyệt đồ thị theo từng 'lớp sóng' hay từng tầng (Level-order)."))
    add(make_bullet("Cơ chế hàng đợi FIFO", "BFS sử dụng cấu trúc hàng đợi (Queue) để lưu trữ các đỉnh đang chờ duyệt. Đỉnh nào được phát hiện trước sẽ được lấy ra thăm trước."))
    add(make_bullet("Mảng đánh dấu và khoảng cách", "Sử dụng tập hợp visited hoặc bảng băm distanceMap để ghi nhận khoảng cách ngắn nhất từ đỉnh nguồn đến từng đỉnh, ngăn ngừa duyệt lại các đỉnh đã thăm."))
    add(make_bullet("Định lý đường đi ngắn nhất", "Trên đồ thị vô hướng không có trọng số (hoặc tất cả các cạnh có trọng số bằng 1), giải thuật BFS luôn đảm bảo tìm ra đường đi ngắn nhất (ít cạnh kết nối nhất) từ đỉnh nguồn đến mọi đỉnh khác."))
    
    add(make_callout("Ứng dụng cốt lõi của BFS trong bài toán",
                     "1. Tìm đường kết nối ngắn nhất (Degrees of Separation): BFS duyệt mở rộng từ người A đến khi chạm tới người B, truy vết mảng parent để dựng lại chính xác chuỗi kết bạn ngắn nhất.\n"
                     "2. Gợi ý bạn mới (Friends of Friends): Các đỉnh ở tầng 1 là bạn trực tiếp; các đỉnh ở tầng 2 chính là bạn của bạn bè mình (nhưng chưa kết bạn với mình). Đây là tập ứng viên kết bạn lý tưởng nhất!"))
                     
    add(make_heading_2("2.4. Giải thuật Duyệt theo chiều sâu (Depth-First Search - DFS)"))
    add(make_para("Giải thuật Duyệt theo chiều sâu (DFS) tiến hành khám phá đồ thị bằng cách đi sâu tối đa theo từng nhánh liên kết cho tới khi không thể đi tiếp được nữa, sau đó quay lui (Backtracking) để khám phá các nhánh khác."))
    add(make_bullet("Cơ chế ngăn xếp LIFO", "DFS được cài đặt tự nhiên bằng kỹ thuật Đệ quy (Call Stack của hệ thống) hoặc ngăn xếp tường minh (Stack)."))
    add(make_bullet("Kiểm soát độ sâu (Depth-limited DFS)", "Để phục vụ bài toán gợi ý bạn bè, nhóm áp dụng giải thuật DFS có giới hạn độ sâu (maxDepth = 2 hoặc 3). Khi duyệt đến giới hạn, thuật toán sẽ quay lui để tìm kiếm các nhánh khác, tránh việc duyệt quá xa khỏi vòng quan hệ quen biết."))
    
    add(make_heading_2("2.5. Các bài toán phân tích mạng xã hội đặc trưng"))
    add(make_heading_3("2.5.1. Bài toán Bạn bè chung (Mutual Friends)"))
    add(make_para("Cho hai người dùng u và v. Tập bạn bè chung Mutual(u, v) được định nghĩa là phần giao của hai tập hợp bạn bè:"))
    add(make_para("Mutual(u, v) = Friends(u) ∩ Friends(v)"))
    add(make_para("Với cấu trúc Set, ta chỉ cần lặp qua tập bạn bè của người có bậc nhỏ hơn và kiểm tra xem có thuộc tập bạn bè của người kia hay không, thời gian thực thi là O(min(deg(u), deg(v)))."))
    
    add(make_heading_3("2.5.2. Bài toán Gợi ý bạn bè & Xếp hạng (Friend Recommendation & Ranking)"))
    add(make_para("Không chỉ dừng lại ở việc tìm ra danh sách ứng viên tầng 2, hệ thống còn xây dựng hàm tính điểm xếp hạng (Ranking Score) để ưu tiên những người có mức độ tương thích cao nhất lên đầu danh sách:"))
    add(make_bullet("Điểm số bạn chung", "Mỗi bạn bè chung đóng góp hệ số trọng số quan trọng nhất (Score += MutualCount * 2.0). Càng có nhiều bạn chung thì xác suất quen biết ngoài đời thực càng cao."))
    add(make_bullet("Điểm tương đồng thuộc tính", "Cộng điểm thưởng nếu hai người dùng có chung ngành học (+0.5 điểm), cùng quê quán (+0.5 điểm) và có các sở thích tương đồng (+0.3 điểm / sở thích chung)."))
    
    add(make_heading_3("2.5.3. Bài toán Bậc phân cách (Six Degrees of Separation)"))
    add(make_para("Thuyết 6 bậc phân cách do nhà văn Frigyes Karinthy đề xuất và được nhà tâm lý học Stanley Milgram thực nghiệm năm 1967 chỉ ra rằng: bất kỳ hai người nào trên thế giới đều có thể kết nối với nhau thông qua chuỗi không quá 6 người quen trung gian. Giải thuật BFS của nhóm cho phép tính toán chính xác số bậc phân cách này giữa hai sinh viên bất kỳ."))
    
    add(make_heading_3("2.5.4. Độ tương đồng Jaccard (Jaccard Similarity)"))
    add(make_para("Hệ số Jaccard đo lường mức độ tương đồng giữa hai tập hợp bạn bè của u và v:"))
    add(make_para("J(u, v) = |Friends(u) ∩ Friends(v)| / |Friends(u) ∪ Friends(v)|"))
    add(make_para("Giá trị J nằm trong khoảng [0, 1]. J = 1 khi hai người có cùng toàn bộ danh sách bạn bè; J = 0 khi không có bất kỳ bạn chung nào."))
    
    add(make_heading_2("2.6. Phân tích độ phức tạp thời gian và không gian"))
    headers_complex = ["Giải thuật / Thao tác", "Thời gian trung bình", "Thời gian xấu nhất", "Bộ nhớ bổ sung", "Ghi chú kỹ thuật"]
    rows_complex = [
        ["Thêm đỉnh / Xóa đỉnh", "O(1) / O(deg(u))", "O(1) / O(V)", "O(1)", "Dựa trên bảng băm HashMap"],
        ["Thêm cạnh / Xóa cạnh", "O(1)", "O(1)", "O(1)", "Thao tác trên LinkedHashSet"],
        ["Kiểm tra bạn bè", "O(1)", "O(1)", "O(1)", "Tra cứu trực tiếp qua Set"],
        ["Tìm bạn bè chung", "O(min(d1, d2))", "O(V)", "O(min(d1, d2))", "d1 = deg(u1), d2 = deg(u2)"],
        ["Gợi ý bạn mới (BFS)", "O(V + E)", "O(V + E)", "O(V)", "Duyệt tầng 2 & Queue"],
        ["Gợi ý bạn mới (DFS)", "O(b^d)", "O(V + E)", "O(d)", "b: hệ số phân nhánh, d: độ sâu"],
        ["Đường đi ngắn nhất (BFS)", "O(V + E)", "O(V + E)", "O(V)", "Tối ưu tuyệt đối trên unweighted graph"],
        ["Thành phần liên thông", "O(V + E)", "O(V + E)", "O(V)", "Duyệt qua toàn bộ đỉnh chưa thăm"],
        ["Top Influencers (K)", "O(V log K)", "O(V log V)", "O(V)", "Sắp xếp theo số lượng bạn bè"]
    ]
    add(make_table(headers_complex, rows_complex, col_widths=[2400, 1700, 1700, 1500, 2200]))
    
    add(make_page_break())
    
    # =========================================================================
    # CHƯƠNG 3
    # =========================================================================
    add(make_heading_1("CHƯƠNG 3: THIẾT KẾ VÀ CÀI ĐẶT HỆ THỐNG"))
    
    add(make_heading_2("3.1. Kiến trúc tổng thể và tổ chức gói phần mềm (Package)"))
    add(make_para("Dự án được thiết kế chặt chẽ theo mô hình phân lớp module hóa (Layered Architecture) với các package chuẩn trong Java:"))
    add(make_bullet("ptit.dsa.socialnetwork.model", "Chứa lớp thực thể User đại diện cho đỉnh (Node) trong mạng xã hội."))
    add(make_bullet("ptit.dsa.socialnetwork.graph", "Chứa lớp SocialGraph quản lý cấu trúc đồ thị vô hướng, danh sách kề và I/O dữ liệu (Do Nguyễn Minh Tiến - B22DVCN308 đảm nhiệm)."))
    add(make_bullet("ptit.dsa.socialnetwork.analysis", "Chứa lớp SocialAnalyzer triển khai toàn bộ các thuật toán phân tích BFS, DFS, Shortest Path, Mutual Friends, Connected Components cùng các lớp kết quả FriendRecommendation, ConnectionPath (Do Đồng Duy Phúc - B22DVCN152 đảm nhiệm)."))
    add(make_bullet("ptit.dsa.socialnetwork.app", "Chứa lớp SocialNetworkApp điều khiển giao diện Menu dòng lệnh tương tác người dùng."))
    add(make_bullet("ptit.dsa.socialnetwork.test", "Chứa lớp SocialNetworkTest thực hiện bộ kiểm thử tự động toàn diện kiểm chứng tất cả các ca kiểm thử."))
    
    add(make_heading_2("3.2. Thiết kế chi tiết các lớp đối tượng"))
    add(make_heading_3("3.2.1. Lớp User (Thực thể người dùng - Người 1 thực hiện)"))
    add(make_para("Lớp User đóng gói toàn bộ thông tin cá nhân và quan hệ bạn bè của một thành viên trong mạng:"))
    add(make_bullet("Thuộc tính riêng (Private fields)", "id (Mã sinh viên/ID duy nhất), name (Họ và tên), age (Tuổi), gender (Giới tính), major (Chuyên ngành), hometown (Quê quán), hobbies (Tập hợp sở thích Set<String>), friendIds (Tập hợp ID bạn bè Set<String>)."))
    add(make_bullet("Phương thức quan trọng", "addFriend(String id), removeFriend(String id), hasFriend(String id), getFriendCount(), equals() và hashCode() override dựa trên thuộc tính ID để đảm bảo tính duy nhất trong bảng băm."))
    
    add(make_heading_3("3.2.2. Lớp SocialGraph (Cấu trúc Đồ thị vô hướng - Người 1 thực hiện)"))
    add(make_para("Lớp SocialGraph quản lý toàn bộ cấu trúc topo của mạng xã hội:"))
    add(make_bullet("Biến thành viên", "userMap (Map<String, User>) lưu danh sách thực thể; adjacencyList (Map<String, Set<String>>) lưu danh sách kề bạn bè hai chiều."))
    add(make_bullet("Phương thức quản trị đồ thị", "addUser(User u), removeUser(String id), addFriendship(String u1, String u2) (thêm cạnh 2 chiều đồng thời), removeFriendship(String u1, String u2), isFriend(String u1, String u2), getFriends(String id), getAdjacencyListAsMap()."))
    add(make_bullet("Phương thức dữ liệu", "loadSampleData() (nạp 12 sinh viên PTIT mẫu), exportToFile(String path), importFromFile(String path)."))
    
    add(make_heading_3("3.2.3. Lớp SocialAnalyzer (Xử lý giải thuật phân tích - Người 2 thực hiện)"))
    add(make_para("Lớp SocialAnalyzer đóng vai trò trái tim giải thuật của đề tài, tiếp nhận đồ thị SocialGraph và thực thi các phân tích chuyên sâu:"))
    add(make_bullet("getMutualFriends(String u1, String u2)", "Trả về danh sách bạn bè chung giữa 2 người bằng phép giao tập hợp."))
    add(make_bullet("recommendFriendsBFS(String userId, int maxResults)", "Duyệt BFS tìm bạn của bạn (tầng 2), tính điểm bạn chung và tương thích, sắp xếp danh sách gợi ý."))
    add(make_bullet("recommendFriendsDFS(String userId, int maxDepth, int maxResults)", "Duyệt DFS giới hạn độ sâu để tìm kiếm gợi ý trên các nhánh sâu."))
    add(make_bullet("findShortestPathBFS(String sourceId, String targetId)", "Tìm đường đi ngắn nhất giữa 2 người, trả về đối tượng ConnectionPath chứa số bước và chuỗi lộ trình."))
    add(make_bullet("findConnectedComponents()", "Duyệt toàn bộ đồ thị để phân nhóm các thành phần liên thông."))
    add(make_bullet("getTopInfluencers(int topK)", "Tìm Top K người dùng có nhiều bạn bè nhất."))
    
    add(make_heading_2("3.3. Cài đặt các giải thuật cốt lõi và giải thích mã nguồn"))
    
    add(make_heading_3("3.3.1. Cài đặt giải thuật Xem bạn bè chung (Mutual Friends)"))
    add(make_para("Thuật toán trích xuất tập bạn bè của hai người dùng và thực hiện phép giao tập hợp:"))
    add(make_code_box([
        "public List<User> getMutualFriends(String userId1, String userId2) {",
        "    if (userId1 == null || userId2 == null) return Collections.emptyList();",
        "    if (!graph.containsUser(userId1) || !graph.containsUser(userId2)) return Collections.emptyList();",
        "",
        "    Set<String> friends1 = graph.getFriendIds(userId1);",
        "    Set<String> friends2 = graph.getFriendIds(userId2);",
        "",
        "    // Thực hiện phép giao hai tập hợp danh sách bạn bè",
        "    Set<String> commonIds = new LinkedHashSet<>(friends1);",
        "    commonIds.retainAll(friends2);",
        "",
        "    List<User> mutualUsers = new ArrayList<>();",
        "    for (String id : commonIds) {",
        "        User u = graph.getUser(id);",
        "        if (u != null) mutualUsers.add(u);",
        "    }",
        "    return mutualUsers;",
        "}"
    ]))
    
    add(make_heading_3("3.3.2. Cài đặt giải thuật Gợi ý bạn mới bằng BFS (Friends of Friends)"))
    add(make_para("Duyệt BFS theo tầng khoảng cách. Chỉ thu thập các ứng viên ở tầng 2 (khoảng cách = 2) chưa kết bạn trực tiếp, sau đó chấm điểm theo số bạn chung và tương đồng thuộc tính:"))
    add(make_code_box([
        "public List<FriendRecommendation> recommendFriendsBFS(String userId, int maxResults) {",
        "    User rootUser = graph.getUser(userId);",
        "    Set<String> directFriends = graph.getFriendIds(userId);",
        "    Map<String, Integer> distanceMap = new HashMap<>();",
        "    Queue<String> queue = new ArrayDeque<>();",
        "",
        "    queue.add(userId);",
        "    distanceMap.put(userId, 0);",
        "    Set<String> candidateIds = new LinkedHashSet<>();",
        "",
        "    while (!queue.isEmpty()) {",
        "        String currentId = queue.poll();",
        "        int currentDist = distanceMap.get(currentId);",
        "        if (currentDist >= 2) continue; // Dừng mở rộng khi vượt quá tầng 2",
        "",
        "        for (String neighborId : graph.getFriendIds(currentId)) {",
        "            if (!distanceMap.containsKey(neighborId)) {",
        "                distanceMap.put(neighborId, currentDist + 1);",
        "                queue.add(neighborId);",
        "                // Tầng 2: Bạn của bạn, chưa kết bạn trực tiếp và không phải chính mình",
        "                if (currentDist + 1 == 2 && !directFriends.contains(neighborId) && !neighborId.equals(userId)) {",
        "                    candidateIds.add(neighborId);",
        "                }",
        "            }",
        "        }",
        "    }",
        "    // Chấm điểm và xếp hạng danh sách gợi ý theo số bạn chung",
        "    List<FriendRecommendation> recommendations = new ArrayList<>();",
        "    for (String candId : candidateIds) {",
        "        User candUser = graph.getUser(candId);",
        "        List<User> mutualList = getMutualFriends(userId, candId);",
        "        double score = mutualList.size() * 2.0 + calculateAffinityScore(rootUser, candUser);",
        "        recommendations.add(new FriendRecommendation(candUser, mutualList.size(), mutualList, score, \"BFS\"));",
        "    }",
        "    Collections.sort(recommendations); // Sắp xếp giảm dần theo độ ưu tiên",
        "    return recommendations.subList(0, Math.min(recommendations.size(), maxResults));",
        "}"
    ]))
    
    add(make_heading_3("3.3.3. Cài đặt giải thuật Tìm đường kết nối ngắn nhất (BFS Shortest Path)"))
    add(make_para("Duyệt BFS từ người nguồn đến người đích, ghi nhận quan hệ đỉnh cha trong parentMap để truy vết chính xác đường đi:"))
    add(make_code_box([
        "public ConnectionPath findShortestPathBFS(String sourceId, String targetId) {",
        "    if (sourceId.equalsIgnoreCase(targetId)) return ConnectionPath.sameUser(graph.getUser(sourceId));",
        "",
        "    Queue<String> queue = new ArrayDeque<>();",
        "    Map<String, String> parentMap = new HashMap<>();",
        "    Set<String> visited = new HashSet<>();",
        "",
        "    queue.add(sourceId);",
        "    visited.add(sourceId);",
        "    parentMap.put(sourceId, null);",
        "    boolean found = false;",
        "",
        "    while (!queue.isEmpty()) {",
        "        String current = queue.poll();",
        "        if (current.equalsIgnoreCase(targetId)) { found = true; break; }",
        "",
        "        for (String neighbor : graph.getFriendIds(current)) {",
        "            if (!visited.contains(neighbor)) {",
        "                visited.add(neighbor);",
        "                parentMap.put(neighbor, current);",
        "                queue.add(neighbor);",
        "                if (neighbor.equalsIgnoreCase(targetId)) { found = true; break; }",
        "            }",
        "        }",
        "        if (found) break;",
        "    }",
        "    if (!found) return ConnectionPath.notConnected(sourceId, targetId);",
        "",
        "    // Truy vết ngược từ targetId về sourceId",
        "    LinkedList<User> path = new LinkedList<>();",
        "    String curr = targetId;",
        "    while (curr != null) {",
        "        path.addFirst(graph.getUser(curr));",
        "        curr = parentMap.get(curr);",
        "    }",
        "    return new ConnectionPath(sourceId, targetId, true, path.size() - 1, path);",
        "}"
    ]))
    
    add(make_page_break())
    
    # =========================================================================
    # CHƯƠNG 4
    # =========================================================================
    add(make_heading_1("CHƯƠNG 4: KẾT QUẢ THỰC NGHIỆM VÀ ĐÁNH GIÁ"))
    
    add(make_heading_2("4.1. Kịch bản thực nghiệm và tập dữ liệu sinh viên PTIT"))
    add(make_para("Để kiểm chứng tính đúng đắn và hiệu năng thực tế của các giải thuật, nhóm đã xây dựng tập dữ liệu mô phỏng gồm 12 sinh viên tiêu biểu đại diện cho các chuyên ngành tại Học viện Công nghệ Bưu chính Viễn thông (CNTT, ATTT, DTVT, Marketing, Kế toán, QTKD) với 15 mối quan hệ bạn bè:"))
    
    headers_users = ["Mã ID", "Họ và tên sinh viên", "Tuổi", "Ngành học", "Quê quán", "Sở thích tiêu biểu"]
    rows_users = [
        ["U01", "Nguyễn Minh Tiến", "22", "CNTT", "Hà Nội", "Lập trình, AI, Bóng đá"],
        ["U02", "Đồng Duy Phúc", "22", "CNTT", "Hải Phòng", "Lập trình, Đồ thị, Âm nhạc"],
        ["U03", "Trần Thu Hà", "21", "ATTT", "Bắc Ninh", "Bảo mật, Đọc sách, Nhiếp ảnh"],
        ["U04", "Lê Hoàng Long", "22", "DTVT", "Nam Định", "Điện tử, Bóng rổ, Game"],
        ["U05", "Phạm Hải Đăng", "23", "CNTT", "Hà Nam", "Web Fullstack, Cầu lông"],
        ["U06", "Vũ Mai Phương", "21", "Marketing", "Hà Nội", "Content, Du lịch, Nhiếp ảnh"],
        ["U07", "Đỗ Quang Huy", "22", "ATTT", "Thái Bình", "Mạng máy tính, Gym, Bóng đá"],
        ["U08", "Ngô Quỳnh Chi", "20", "CNTT", "Quảng Ninh", "UI/UX, Vẽ, Đọc sách"],
        ["U09", "Bùi Quốc Anh", "22", "DTVT", "Nghệ An", "IoT, Robotics, Bóng rổ"],
        ["U10", "Hoàng Kim Yến", "21", "Kế toán", "Phú Thọ", "Tài chính, Nấu ăn, Du lịch"],
        ["U11", "Đặng Văn Nam", "23", "QTKD", "Thanh Hóa", "Kinh doanh, Bơi lội (Nhóm độc lập)"],
        ["U12", "Lê Thúy Nga", "22", "QTKD", "Hải Dương", "Khởi nghiệp, Yoga (Nhóm độc lập)"]
    ]
    add(make_table(headers_users, rows_users, col_widths=[900, 2200, 700, 1100, 1400, 3200]))
    
    add(make_para("Topo mạng lưới kết bạn được thiết lập như sau:"))
    add(make_bullet("Cụm mạng chính (10 sinh viên)", "(U01-U02), (U01-U03), (U01-U05), (U02-U03), (U02-U04), (U03-U06), (U03-U07), (U04-U07), (U04-U09), (U05-U08), (U06-U08), (U06-U10), (U07-U08), (U08-U10)."))
    add(make_bullet("Cụm mạng độc lập (2 sinh viên)", "(U11-U12). Cụm này không có liên kết nào với cụm chính, dùng để kiểm thử khả năng xử lý đồ thị không liên thông."))
    
    add(make_heading_2("4.2. Kết quả chạy thực tế của các chức năng"))
    
    add(make_heading_3("4.2.1. Kết quả Chức năng 1: Xem bạn bè chung (Mutual Friends)"))
    add(make_para("Thử nghiệm với cặp đôi Nguyễn Minh Tiến (U01) và Đồng Duy Phúc (U02):"))
    add(make_code_box([
        "--- [CHỨC NĂNG 1] TÌM BẠN BÈ CHUNG GIỮA 2 NGƯỜI ---",
        "Nhập ID người thứ nhất: U01",
        "Nhập ID người thứ hai: U02",
        "--------------------------------------------------------------------------------",
        "🤝 BẠN BÈ CHUNG GIỮA: Nguyễn Minh Tiến [U01] VÀ Đồng Duy Phúc [U02]",
        "Số lượng bạn chung: 1 người",
        "    1. [U03] Trần Thu Hà            | Ngành: ATTT         | Tuổi: 21",
        "--------------------------------------------------------------------------------"
    ]))
    add(make_para("Đánh giá: Kết quả hoàn toàn chính xác. Cả Tiến và Phúc đều có bạn chung trực tiếp là bạn Trần Thu Hà (U03)."))
    
    add(make_heading_3("4.2.2. Kết quả Chức năng 2: Gợi ý bạn mới bằng BFS (Friends of Friends)"))
    add(make_para("Thử nghiệm gợi ý kết bạn cho bạn Nguyễn Minh Tiến (U01):"))
    add(make_code_box([
        "--- [CHỨC NĂNG 2] GỢI Ý BẠN MỚI BẰNG THUẬT TOÁN BFS (FRIENDS OF FRIENDS) ---",
        "Nhập ID người dùng cần nhận gợi ý kết bạn: U01",
        "--------------------------------------------------------------------------------",
        "💡 KẾT QUẢ GỢI Ý KẾT BẠN (BFS) CHO: Nguyễn Minh Tiến [U01]",
        "1. [U04] Lê Hoàng Long        | Số bạn chung: 1  | Thuật toán: BFS (Friends of Friends)",
        "   -> Bạn chung: Đồng Duy Phúc",
        "2. [U06] Vũ Mai Phương       | Số bạn chung: 1  | Thuật toán: BFS (Friends of Friends)",
        "   -> Bạn chung: Trần Thu Hà",
        "3. [U07] Đỗ Quang Huy         | Số bạn chung: 1  | Thuật toán: BFS (Friends of Friends)",
        "   -> Bạn chung: Trần Thu Hà",
        "4. [U08] Ngô Quỳnh Chi        | Số bạn chung: 1  | Thuật toán: BFS (Friends of Friends)",
        "   -> Bạn chung: Phạm Hải Đăng",
        "--------------------------------------------------------------------------------"
    ]))
    add(make_para("Đánh giá: Thuật toán BFS lọc chính xác những người ở tầng 2 (bạn của bạn của Tiến nhưng Tiến chưa kết bạn), đồng thời chỉ rõ bạn chung trung gian là ai."))
    
    add(make_heading_3("4.2.3. Kết quả Chức năng 3: Gợi ý bạn mới bằng DFS (Duyệt theo chiều sâu)"))
    add(make_para("Thử nghiệm duyệt DFS với độ sâu maxDepth = 3 cho Nguyễn Minh Tiến (U01):"))
    add(make_code_box([
        "--- [CHỨC NĂNG 2] GỢI Ý BẠN MỚI BẰNG THUẬT TOÁN DFS (DUYỆT THEO CHIỀU SÂU) ---",
        "Nhập ID người dùng cần nhận gợi ý kết bạn: U01",
        "--------------------------------------------------------------------------------",
        "💡 KẾT QUẢ GỢI Ý KẾT BẠN (DFS - Độ sâu 3) CHO: Nguyễn Minh Tiến [U01]",
        "1. [U04] Lê Hoàng Long        | Số bạn chung: 1  | Thuật toán: DFS (Deep Network Exploration)",
        "2. [U06] Vũ Mai Phương       | Số bạn chung: 1  | Thuật toán: DFS (Deep Network Exploration)",
        "3. [U07] Đỗ Quang Huy         | Số bạn chung: 1  | Thuật toán: DFS (Deep Network Exploration)",
        "4. [U08] Ngô Quỳnh Chi        | Số bạn chung: 1  | Thuật toán: DFS (Deep Network Exploration)",
        "5. [U09] Bùi Quốc Anh        | Số bạn chung: 0  | Thuật toán: DFS (Deep Network Exploration)",
        "--------------------------------------------------------------------------------"
    ]))
    add(make_para("Đánh giá: Khác với BFS chỉ dừng ở tầng 2, DFS độ sâu 3 đã vươn tới Bùi Quốc Anh (U09) thông qua chuỗi Tiến -> Phúc -> Long -> Quốc Anh, mở rộng phạm vi khám phá mạng lưới."))
    
    add(make_heading_3("4.2.4. Kết quả Chức năng 4: Tìm đường kết nối ngắn nhất bằng BFS"))
    add(make_para("Trường hợp 1: Hai người dùng trong cùng thành phần liên thông (Nguyễn Minh Tiến U01 đến Hoàng Kim Yến U10):"))
    add(make_code_box([
        "--- [CHỨC NĂNG 3] TÌM ĐƯỜNG KẾT NỐI NGẮN NHẤT BẰNG BFS (DEGREES OF SEPARATION) ---",
        "Nhập ID người bắt đầu (Source): U01",
        "Nhập ID người đích đến (Target): U10",
        "--------------------------------------------------------------------------------",
        "🔍 KẾT QUẢ TÌM ĐƯỜNG KẾT NỐI:",
        "Khoảng cách kết nối: 3 bước (Degrees of Separation = 3)",
        "Lộ trình kết nối: Nguyễn Minh Tiến (U01) ──> Trần Thu Hà (U03) ──> Vũ Mai Phương (U06) ──> Hoàng Kim Yến (U10)",
        "--------------------------------------------------------------------------------"
    ]))
    add(make_para("Trường hợp 2: Hai người dùng ở hai thành phần liên thông rời nhau (Nguyễn Minh Tiến U01 đến Đặng Văn Nam U11):"))
    add(make_code_box([
        "--- [CHỨC NĂNG 3] TÌM ĐƯỜNG KẾT NỐI NGẮN NHẤT BẰNG BFS ---",
        "Nhập ID người bắt đầu (Source): U01",
        "Nhập ID người đích đến (Target): U11",
        "--------------------------------------------------------------------------------",
        "🔍 KẾT QUẢ TÌM ĐƯỜNG KẾT NỐI:",
        "Không có đường kết nối nào giữa người dùng 'U01' và 'U11' (Khác thành phần liên thông).",
        "--------------------------------------------------------------------------------"
    ]))
    add(make_para("Đánh giá: Thuật toán BFS tìm ra lộ trình ngắn nhất chính xác tuyệt đối với 3 bước chuyển tiếp, đồng thời xử lý an toàn ngoại lệ khi hai đỉnh thuộc hai thành phần liên thông tách rời."))
    
    add(make_heading_3("4.2.5. Kết quả các tính năng phân tích mở rộng"))
    add(make_para("Thống kê Top Influencers và Thành phần liên thông trên toàn bộ mạng xã hội:"))
    add(make_code_box([
        "⭐ TOP 5 NGƯỜI DÙNG CÓ NHIỀU BẠN BÈ NHẤT MẠNG XÃ HỘI (DEGREE CENTRALITY):",
        "  Top 1: [U08] Ngô Quỳnh Chi        - Số bạn bè: 4 (CNTT)",
        "  Top 2: [U03] Trần Thu Hà          - Số bạn bè: 4 (ATTT)",
        "  Top 3: [U06] Vũ Mai Phương        - Số bạn bè: 3 (Marketing)",
        "  Top 4: [U07] Đỗ Quang Huy         - Số bạn bè: 3 (ATTT)",
        "  Top 5: [U04] Lê Hoàng Long        - Số bạn bè: 3 (DTVT)",
        "",
        "🌐 MẠNG XÃ HỘI CÓ TỔNG CỘNG: 2 THÀNH PHẦN LIÊN THÔNG (NHÓM ĐỘC LẬP):",
        "  Nhóm 1 (10 thành viên): Tiến, Phúc, Hà, Long, Đăng, Phương, Huy, Chi, Quốc Anh, Kim Yến",
        "  Nhóm 2 (2 thành viên) : Nam, Nga"
    ]))
    
    add(make_heading_2("4.3. Đánh giá kiểm thử tự động toàn diện"))
    add(make_para("Lớp kiểm thử tự động SocialNetworkTest đã thực thi 8 ca kiểm thử độc lập bao quát toàn bộ các yêu cầu của đề tài:"))
    
    headers_tests = ["STT Test", "Tên ca kiểm thử (Test Case)", "Đầu vào (Input)", "Kết quả kỳ vọng", "Kết quả thực tế", "Trạng thái"]
    rows_tests = [
        ["TEST 1", "Khởi tạo & Cấu trúc đồ thị", "loadSampleData()", "12 users, 15 edges", "12 users, 15 edges", "PASS (100%)"],
        ["TEST 2", "Xem bạn bè chung (Mutual)", "U01 (Tiến) & U02 (Phúc)", "1 bạn chung là U03", "Tìm thấy đúng U03", "PASS (100%)"],
        ["TEST 3", "Gợi ý bạn mới bằng BFS", "U01 (Tiến), max=5", "Gợi ý ứng viên tầng 2", "Gợi ý U04, U06, U07, U08", "PASS (100%)"],
        ["TEST 4", "Gợi ý bạn mới bằng DFS", "U01 (Tiến), depth=3", "Gợi ý ứng viên sâu", "Gợi ý U04, U06, U07, U08, U09", "PASS (100%)"],
        ["TEST 5", "Đường đi ngắn nhất BFS", "U01 (Tiến) -> U10 (Yến)", "Độ dài = 3 bước", "Khoảng cách = 3 bước", "PASS (100%)"],
        ["TEST 6", "Xử lý đồ thị không liên thông", "U01 (Tiến) -> U11 (Nam)", "notConnected = true", "Báo không có đường đi", "PASS (100%)"],
        ["TEST 7", "Phát hiện thành phần liên thông", "Toàn bộ đồ thị", "2 thành phần độc lập", "2 thành phần (10 và 2)", "PASS (100%)"],
        ["TEST 8", "Lưu & Đọc dữ liệu File", "Export/Import file", "Dữ liệu trùng khớp 100%", "Dữ liệu toàn vẹn", "PASS (100%)"]
    ]
    add(make_table(headers_tests, rows_tests, col_widths=[900, 2200, 1600, 1600, 1600, 1600]))
    
    add(make_callout("Kết quả kiểm thử", "Tỉ lệ thành công của bộ kiểm thử đạt 8/8 Test Cases (100%). Hệ thống hoạt động hoàn toàn ổn định, chính xác và không phát sinh bất kỳ lỗi ngoại lệ nào trong suốt quá trình chạy."))
    
    add(make_page_break())
    
    # =========================================================================
    # CHƯƠNG 5
    # =========================================================================
    add(make_heading_1("CHƯƠNG 5: KẾT LUẬN VÀ HƯỚNG PHÁT TRIỂN"))
    
    add(make_heading_2("5.1. Kết quả đạt được so với yêu cầu đề bài"))
    add(make_para("Sau thời gian nghiên cứu và thực hiện nghiêm túc, Nhóm 12 đã hoàn thành toàn diện và vượt mức các yêu cầu đề bài đặt ra:"))
    add(make_bullet("1. Xây dựng hoàn chỉnh mô hình đồ thị vô hướng", "Tổ chức dữ liệu bài bản với class User và SocialGraph sử dụng danh sách kề HashMap<String, Set<String>> tối ưu bộ nhớ O(V + E) và tốc độ tra cứu O(1)."))
    add(make_bullet("2. Hoàn thành xuất sắc 3 chức năng cốt lõi", "Cài đặt thành công chức năng Xem bạn bè chung, Gợi ý bạn mới bằng BFS/DFS có xếp hạng ưu tiên, và Tìm đường kết nối ngắn nhất giữa 2 người dùng kèm truy vết trực quan."))
    add(make_bullet("3. Mở rộng tính năng phân tích xã hội giá trị", "Bổ sung phân tích thành phần liên thông (Connected Components), Top Influencers (Degree Centrality), và tính hệ số tương đồng bạn bè Jaccard."))
    add(make_bullet("4. Kiến trúc phần mềm chuẩn mực", "Mã nguồn tuân thủ nguyên tắc OOP, Clean Code, có đầy đủ bộ kiểm thử tự động và giao diện dòng lệnh Menu trực quan."))
    
    add(make_heading_2("5.2. Đánh giá ưu điểm và hạn chế của hệ thống"))
    add(make_bullet("Ưu điểm nổi bật", "Tốc độ xử lý cực nhanh nhờ cấu trúc bảng băm; thuật toán BFS đảm bảo đường đi tìm được luôn là ngắn nhất; giao diện thân thiện, hỗ trợ tiếng Việt có dấu; có sẵn bộ dữ liệu mẫu sinh viên PTIT phong phú."))
    add(make_bullet("Hạn chế hiện tại", "Đồ thị hiện tại là đồ thị không trọng số (mọi mối quan hệ bạn bè có trọng số bình đẳng như nhau); hệ thống chạy trên giao diện dòng lệnh Console nên chưa có trực quan hóa đồ họa 2D/3D các node và edge."))
    
    add(make_heading_2("5.3. Hướng phát triển trong tương lai"))
    add(make_para("Trong giai đoạn tiếp theo, nhóm định hướng mở rộng đề tài theo các hướng sau:"))
    add(make_bullet("Nâng cấp Đồ thị có trọng số (Weighted Graph)", "Gán trọng số cho từng cạnh dựa trên tần suất tương tác, số lượng tin nhắn trao đổi hoặc lịch sử bình luận giữa hai người dùng. Khi đó, bài toán tìm đường kết nối ngắn nhất sẽ được nâng cấp lên giải thuật Dijkstra hoặc A*."))
    add(make_bullet("Xây dựng giao diện Web / GUI đồ họa", "Tích hợp thư viện trực quan hóa đồ thị như Vis.js, D3.js hoặc JavaFX Graph để hiển thị trực quan các nút mạng xã hội dưới dạng đồ họa động sinh động."))
    add(make_bullet("Ứng dụng Trí tuệ nhân tạo (AI/Machine Learning)", "Áp dụng các mô hình Graph Neural Networks (GNN) và thuật toán phân cụm cộng đồng (Community Detection như Louvain Algorithm) để dự đoán xu hướng kết bạn và gợi ý nhóm sở thích chính xác hơn."))
    
    add(make_page_break())
    
    # =========================================================================
    # TÀI LIỆU THAM KHẢO
    # =========================================================================
    add(make_heading_1("TÀI LIỆU THAM KHẢO"))
    refs = [
        "[1] Học viện Công nghệ Bưu chính Viễn thông, Giáo trình Cấu trúc dữ liệu và Giải thuật, NXB Bưu điện, Hà Nội, 2021.",
        "[2] Thomas H. Cormen, Charles E. Leiserson, Ronald L. Rivest, Clifford Stein, Introduction to Algorithms (4th Edition), MIT Press, 2022.",
        "[3] Robert Sedgewick, Kevin Wayne, Algorithms (4th Edition), Addison-Wesley Professional, 2011.",
        "[4] David Easley, Jon Kleinberg, Networks, Crowds, and Markets: Reasoning about a Highly Connected World, Cambridge University Press, 2010.",
        "[5] Oracle Corporation, Java Platform, Standard Edition 17 / 21 API Specification, docs.oracle.com.",
        "[6] Stanley Milgram, 'The Small World Problem', Psychology Today, Vol. 2, pp. 60-67, 1967.",
        "[7] J. Leskovec, A. Rajaraman, J. D. Ullman, Mining of Massive Datasets, Cambridge University Press, 2014."
    ]
    for r in refs:
        add(make_para(r, bold=False, italic=False, align="both", indent=False, space_after='100'))
        
    # Finally, append Section 2's sectPr at the very end of body
    # Section 2 has NO borders, standard A4 margins
    sectPr2 = create_element('sectPr')
    sub_element(sectPr2, 'pgSz', w='11906', h='16838') # A4 size
    sub_element(sectPr2, 'pgMar', top='1134', right='1134', bottom='1134', left='1701', header='720', footer='720', gutter='0')
    sub_element(sectPr2, 'cols', space='720')
    sub_element(sectPr2, 'docGrid', linePitch='360')
    add(sectPr2)
    
    # Write back to docx_extracted/word/document.xml
    tree.write('docx_extracted/word/document.xml', encoding='utf-8', xml_declaration=True)
    print("Updated docx_extracted/word/document.xml successfully!")
    
    # Now repack into 'Phân tích mạng xã hội bằng đồ thị.docx'
    docx_target = 'Phân tích mạng xã hội bằng đồ thị.docx'
    with zipfile.ZipFile(docx_target, 'w', zipfile.ZIP_DEFLATED) as z_out:
        for foldername, subfolders, filenames in os.walk('docx_extracted'):
            for filename in filenames:
                filepath = os.path.join(foldername, filename)
                arcname = os.path.relpath(filepath, 'docx_extracted')
                z_out.write(filepath, arcname)
                
    print(f"Repacked complete document into '{docx_target}' successfully!")
    print(f"Final file size: {os.path.getsize(docx_target)} bytes.")

if __name__ == '__main__':
    build_report()
