package com.acme.transport.crewing.restbreak.claim

import kotlin.test.Test
import kotlin.test.assertEquals

class StrictRestBreakClaimViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newStrictRestBreakClaim("code-330", "title-662")
        val view = toStrictRestBreakClaimView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toStrictRestBreakClaimViews(mutableListOf<StrictRestBreakClaim>(item, item)).size, "views")
    }
}
