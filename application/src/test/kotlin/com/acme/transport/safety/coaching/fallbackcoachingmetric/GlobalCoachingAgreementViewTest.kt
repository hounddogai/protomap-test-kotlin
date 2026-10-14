package com.acme.transport.safety.coaching.fallbackcoachingmetric

import kotlin.test.Test
import kotlin.test.assertEquals

class GlobalCoachingAgreementViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newGlobalCoachingAgreement("code-686", "title-273")
        val view = toGlobalCoachingAgreementView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toGlobalCoachingAgreementViews(mutableListOf<GlobalCoachingAgreement>(item, item)).size, "views")
    }
}
