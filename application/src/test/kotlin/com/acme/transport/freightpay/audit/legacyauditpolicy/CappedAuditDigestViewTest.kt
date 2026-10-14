package com.acme.transport.freightpay.audit.legacyauditpolicy

import kotlin.test.Test
import kotlin.test.assertEquals

class CappedAuditDigestViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newCappedAuditDigest("code-270", "name-986")
        val view = toCappedAuditDigestView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toCappedAuditDigestViews(mutableListOf<CappedAuditDigest>(item, item)).size, "views")
    }
}
