package com.acme.transport.freightpay.audit.slot

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class RollingAuditMappingTest {
    @Test
    fun `starts in created`() {
        val item = newRollingAuditMapping("code-734", "name-485")
        assertEquals(RollingAuditMappingStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to retired`() {
        val item = newRollingAuditMapping("code-734", "name-485")
        try {
            item.moveTo(RollingAuditMappingStatus.RETIRED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(RollingAuditMappingStatus.RETIRED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newRollingAuditMapping("code-734", "name-485")
        item.status = RollingAuditMappingStatus.RETIRED
        assertFails("move") { item.moveTo(RollingAuditMappingStatus.CREATED) }
    }
}
