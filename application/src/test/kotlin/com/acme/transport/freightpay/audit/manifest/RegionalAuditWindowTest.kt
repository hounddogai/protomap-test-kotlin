package com.acme.transport.freightpay.audit.manifest

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class RegionalAuditWindowTest {
    @Test
    fun `starts in created`() {
        val item = newRegionalAuditWindow("code-685", "name-684")
        assertEquals(RegionalAuditWindowStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to validated`() {
        val item = newRegionalAuditWindow("code-685", "name-684")
        try {
            item.moveTo(RegionalAuditWindowStatus.VALIDATED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(RegionalAuditWindowStatus.VALIDATED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newRegionalAuditWindow("code-685", "name-684")
        item.status = RegionalAuditWindowStatus.RETIRED
        assertFails("move") { item.moveTo(RegionalAuditWindowStatus.CREATED) }
    }
}
