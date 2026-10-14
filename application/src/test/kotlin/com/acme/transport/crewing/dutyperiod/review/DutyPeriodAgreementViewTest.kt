package com.acme.transport.crewing.dutyperiod.review

import kotlin.test.Test
import kotlin.test.assertEquals

class DutyPeriodAgreementViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newDutyPeriodAgreement("code-983", "label-493")
        val view = toDutyPeriodAgreementView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toDutyPeriodAgreementViews(mutableListOf<DutyPeriodAgreement>(item, item)).size, "views")
    }
}
