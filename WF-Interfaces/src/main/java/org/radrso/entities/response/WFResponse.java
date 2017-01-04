package org.radrso.entities.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * Created by raomengnan on 17-1-4.
 */
@AllArgsConstructor
@NoArgsConstructor
@Data
@ToString
public class WFResponse {
    private int    code;
    private String msg;
    private Object response;
}
