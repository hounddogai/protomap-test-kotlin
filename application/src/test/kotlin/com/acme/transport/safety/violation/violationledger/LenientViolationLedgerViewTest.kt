package com.acme.transport.safety.violation.violationledger

import kotlin.test.Test
import kotlin.test.assertEquals

class LenientViolationLedgerViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newLenientViolationLedger("code-888", "name-226")
        val view = toLenientViolationLedgerView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toLenientViolationLedgerViews(mutableListOf<LenientViolationLedger>(item, item)).size, "views")
    }
}
