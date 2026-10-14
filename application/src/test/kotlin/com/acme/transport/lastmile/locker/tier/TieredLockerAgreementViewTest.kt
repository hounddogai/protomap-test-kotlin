package com.acme.transport.lastmile.locker.tier

import kotlin.test.Test
import kotlin.test.assertEquals

class TieredLockerAgreementViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newTieredLockerAgreement("code-208", "title-389")
        val view = toTieredLockerAgreementView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toTieredLockerAgreementViews(mutableListOf<TieredLockerAgreement>(item, item)).size, "views")
    }
}
