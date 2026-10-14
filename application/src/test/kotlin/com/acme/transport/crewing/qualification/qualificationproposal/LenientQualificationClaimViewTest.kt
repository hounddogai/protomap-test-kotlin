package com.acme.transport.crewing.qualification.qualificationproposal

import kotlin.test.Test
import kotlin.test.assertEquals

class LenientQualificationClaimViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newLenientQualificationClaim("code-250", "name-834")
        val view = toLenientQualificationClaimView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toLenientQualificationClaimViews(mutableListOf<LenientQualificationClaim>(item, item)).size, "views")
    }
}
