package lionel.meethere.news.vo;

import lionel.meethere.user.vo.UserVO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class NewsCatalogVO {

    private Integer id;

    private UserVO admin;

    private String title;

    private String image;

    private String createTime;
}