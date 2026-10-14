package com.acme.transport.freightpay.audit.auditrollup

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class RegionalAuditRollupTest {
    @Test
    fun `starts in created`() {
        val item = newRegionalAuditRollup("code-648", "name-755")
        assertEquals(RegionalAuditRollupStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to retired`() {
        val item = newRegionalAuditRollup("code-648", "name-755")
        try {
            item.moveTo(RegionalAuditRollupStatus.RETIRED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(RegionalAuditRollupStatus.RETIRED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newRegionalAuditRollup("code-648", "name-755")
        item.status = RegionalAuditRollupStatus.RETIRED
        assertFails("move") { item.moveTo(RegionalAuditRollupStatus.CREATED) }
    }
}
