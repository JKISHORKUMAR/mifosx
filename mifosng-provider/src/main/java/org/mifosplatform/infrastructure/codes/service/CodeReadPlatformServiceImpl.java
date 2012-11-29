package org.mifosplatform.infrastructure.codes.service;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;

import org.mifosng.platform.exceptions.FundNotFoundException;
import org.mifosng.platform.infrastructure.TenantAwareRoutingDataSource;
import org.mifosplatform.infrastructure.codes.data.CodeData;
import org.mifosplatform.infrastructure.security.service.PlatformSecurityContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;

@Service
public class CodeReadPlatformServiceImpl implements CodeReadPlatformService {

    private final JdbcTemplate jdbcTemplate;
    private final PlatformSecurityContext context;

    @Autowired
    public CodeReadPlatformServiceImpl(final PlatformSecurityContext context, final TenantAwareRoutingDataSource dataSource) {
        this.context = context;
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    private static final class CodeMapper implements RowMapper<CodeData> {

        public String schema() {
            return " c.id as id, c.code_name as code_name, c.is_system_defined as systemDefined from m_code c ";
        }

        @Override
        public CodeData mapRow(final ResultSet rs, @SuppressWarnings("unused") final int rowNum) throws SQLException {

            final Long id = rs.getLong("id");
            final String code_name = rs.getString("code_name");
            final boolean systemDefined = rs.getBoolean("systemDefined");
            
            return new CodeData(id, code_name, systemDefined);
        }
    }

    @Override
    public Collection<CodeData> retrieveAllCodes() {
        context.authenticatedUser();

        final CodeMapper rm = new CodeMapper();
        final String sql = "select " + rm.schema() + " order by c.code_name";

        return this.jdbcTemplate.query(sql, rm, new Object[] {});
    }

    @Override
    public CodeData retrieveCode(Long codeId) {
        try {
            context.authenticatedUser();

            final CodeMapper rm = new CodeMapper();
            final String sql = "select " + rm.schema() + " where c.id = ?";

            return this.jdbcTemplate.queryForObject(sql, rm, new Object[] { codeId });
        } catch (EmptyResultDataAccessException e) {
            throw new FundNotFoundException(codeId);
        }
    }
}