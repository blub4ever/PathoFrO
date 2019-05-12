package com.patho.main.model.dto;

import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Immutable;

import javax.persistence.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

@NamedNativeQuery(
        name = "AccountingData.findAllBetweenDates",
        query = "With totals as " +
                "( " +
                "select  tk.receiptDate as creationdate , piz, get_slide_count(s.slideid) as scount, " +
                "bl.id as blockid, " +
                "sm.id as sampleid, " +
                "case when s.slideid ilike '%HE%' then true else false end as he, " +
                "case when s.slideid ilike '%HE%' and (sm.material in ('Bulbi', 'Exenteratio', 'Glaskörperaspirat') or (s.slidelabeltext = '') IS false or tk.commentary ilike '%Depigmentierung%' or tk.commentary ilike '%Entkalkung%' or tk.commentary ilike '%Handeinbettung%') then true else false end as he2, " +
                "case when s.slideid not ilike '%HE%' and sp.type not like 'IMMUN' then true else false end as pas, " +
                "case when sp.type like 'IMMUN' then true else false end as imu " +
                "FROM slide s " +
                "left join stainingprototype sp on s.slideprototype_id = sp.id " +
                "left join block bl on s.parent_id = bl.id " +
                "left join sample sm on bl.parent_id = sm.id " +
                "left join task tk on sm.parent_id = tk.id " +
                "left join patient pt on tk.parent_id = pt.id " +
                ") " +
                "SELECT row_number() OVER () AS id, date_trunc( 'day' ,creationdate)\\:\\:date date, piz, sum(scount) as slidecount, " +
                "count(distinct(blockid)) as blockcount, " +
                "count(distinct(sampleid)) as samplecount," +
                "sum(case when he and not he2 then scount else 0 end) as \"4800\" , " +
                "sum(case when he2 then scount else 0 end)  \"4802\", " +
                "sum(case when pas then scount else 0 end)  \"4815\", " +
                "sum(case when imu then scount else 0 end)  \"4815Imu\" " +
                "FROM totals " +
                "where creationdate >= :fromDate and creationdate <= :toDate " +
                "group by 2,3 " +
                "order by 2,3; ",
        resultSetMapping = "AccountingData"
)
@SqlResultSetMapping(
        name = "AccountingData",
        classes = @ConstructorResult(
                targetClass = AccountingData.class,
                columns = {
                        @ColumnResult(name = "id", type = Long.class),
                        @ColumnResult(name = "date", type = LocalDate.class),
                        @ColumnResult(name = "piz", type = String.class),
                        @ColumnResult(name = "slidecount", type = Integer.class),
                        @ColumnResult(name = "4800", type = Integer.class),
                        @ColumnResult(name = "4802", type = Integer.class),
                        @ColumnResult(name = "4815", type = Integer.class),
                        @ColumnResult(name = "4815Imu", type = Integer.class),
                        @ColumnResult(name = "blockcount", type = Integer.class),
                        @ColumnResult(name = "samplecount", type = Integer.class)
                }
        )
)
@Entity
@Immutable
@Getter
@Setter
public class AccountingData {

    @Id
    private long id;
    private LocalDate date;
    private int year;
    private int month;
    private String piz;
    private int v4800;
    private int v4802;
    private int v4015;
    private int v4815Imu;

    /**
     * Sample count
     */
    private int sampleCount;

    /**
     * Block count
     */

    private int blockCount;
    /**
     * Slide count
     */
    private int slideCount;

    public AccountingData(long id, LocalDate date, String piz, int slideCount, int v4800, int v4802, int v4015, int v4815Imu, int blockcount, int samplecount) {
        this.id = id;
        try {
            DateTimeFormatter format = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            this.date = date;
            this.year = this.date.getYear();
            this.month = this.date.getMonth().getValue();
        } catch (DateTimeParseException e) {
        }

        this.piz = piz;
        this.v4800 = v4800;
        this.v4802 = v4802;
        this.v4015 = v4015;
        this.v4815Imu = v4815Imu;
        this.blockCount = blockcount;
        this.sampleCount = samplecount;
        this.slideCount = slideCount;

    }
}
