package com.acme.transport.freightpay.audit.plan

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class GlobalAuditClaimTest {
    @Test
    fun `starts in created`() {
        val item = newGlobalAuditClaim("code-416", "label-574")
        assertEquals(GlobalAuditClaimStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to published`() {
        val item = newGlobalAuditClaim("code-416", "label-574")
        try {
            item.moveTo(GlobalAuditClaimStatus.PUBLISHED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(GlobalAuditClaimStatus.PUBLISHED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newGlobalAuditClaim("code-416", "label-574")
        item.status = GlobalAuditClaimStatus.RETIRED
        assertFails("move") { item.moveTo(GlobalAuditClaimStatus.CREATED) }
    }
}
