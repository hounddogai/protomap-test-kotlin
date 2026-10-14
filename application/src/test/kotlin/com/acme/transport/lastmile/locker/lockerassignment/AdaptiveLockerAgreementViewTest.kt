package com.acme.transport.lastmile.locker.lockerassignment

import kotlin.test.Test
import kotlin.test.assertEquals

class AdaptiveLockerAgreementViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newAdaptiveLockerAgreement("code-710", "name-889")
        val view = toAdaptiveLockerAgreementView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toAdaptiveLockerAgreementViews(mutableListOf<AdaptiveLockerAgreement>(item, item)).size, "views")
    }
}
