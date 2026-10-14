package com.acme.transport.freightpay.audit.overrideitem

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class PrimaryAuditOverrideTest {
    @Test
    fun `starts in created`() {
        val item = newPrimaryAuditOverride("code-209", "label-170")
        assertEquals(PrimaryAuditOverrideStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to published`() {
        val item = newPrimaryAuditOverride("code-209", "label-170")
        try {
            item.moveTo(PrimaryAuditOverrideStatus.PUBLISHED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(PrimaryAuditOverrideStatus.PUBLISHED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newPrimaryAuditOverride("code-209", "label-170")
        item.status = PrimaryAuditOverrideStatus.RETIRED
        assertFails("move") { item.moveTo(PrimaryAuditOverrideStatus.CREATED) }
    }
}
