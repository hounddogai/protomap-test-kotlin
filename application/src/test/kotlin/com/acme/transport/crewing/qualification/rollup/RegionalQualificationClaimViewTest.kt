package com.acme.transport.crewing.qualification.rollup

import kotlin.test.Test
import kotlin.test.assertEquals

class RegionalQualificationClaimViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newRegionalQualificationClaim("code-318", "name-296")
        val view = toRegionalQualificationClaimView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toRegionalQualificationClaimViews(mutableListOf<RegionalQualificationClaim>(item, item)).size, "views")
    }
}
