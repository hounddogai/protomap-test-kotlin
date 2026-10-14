package com.acme.transport.safety.nearmiss.batch

import kotlin.test.Test
import kotlin.test.assertEquals

class LenientNearMissAgreementViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newLenientNearMissAgreement("code-379", "name-429")
        val view = toLenientNearMissAgreementView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toLenientNearMissAgreementViews(mutableListOf<LenientNearMissAgreement>(item, item)).size, "views")
    }
}
