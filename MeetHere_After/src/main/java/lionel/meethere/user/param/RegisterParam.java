package lionel.meethere.user.param;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotNull;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RegisterParam {

    @NotNull
    @Length(min = 1, max = 20)
    private String username;

    @NotNull
    @Length(min = 8, max = 20)
    private String password;

    @Length(min = 11, max = 11)
    private String telephone;

}
