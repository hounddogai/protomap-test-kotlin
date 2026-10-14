package com.acme.transport.freightpay.audit.auditbudget

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class AdaptiveAuditBundleTest {
    @Test
    fun `starts in new`() {
        val item = newAdaptiveAuditBundle("code-108", "label-190")
        assertEquals(AdaptiveAuditBundleStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to failed`() {
        val item = newAdaptiveAuditBundle("code-108", "label-190")
        try {
            item.moveTo(AdaptiveAuditBundleStatus.FAILED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(AdaptiveAuditBundleStatus.FAILED, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newAdaptiveAuditBundle("code-108", "label-190")
        item.status = AdaptiveAuditBundleStatus.FAILED
        assertFails("move") { item.moveTo(AdaptiveAuditBundleStatus.NEW) }
    }
}
