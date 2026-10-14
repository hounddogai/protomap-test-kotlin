package com.acme.transport.crewing.qualification.milestone

import kotlin.test.Test
import kotlin.test.assertEquals

class QualificationClaimViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newQualificationClaim("code-515", "title-360")
        val view = toQualificationClaimView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toQualificationClaimViews(mutableListOf<QualificationClaim>(item, item)).size, "views")
    }
}
