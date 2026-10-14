package com.acme.transport.lastmile.locker.budget

import kotlin.test.Test
import kotlin.test.assertEquals

class ScheduledLockerAgreementViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newScheduledLockerAgreement("code-500", "title-694")
        val view = toScheduledLockerAgreementView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toScheduledLockerAgreementViews(mutableListOf<ScheduledLockerAgreement>(item, item)).size, "views")
    }
}
