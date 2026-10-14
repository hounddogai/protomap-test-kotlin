package com.acme.transport.lastmile.attempt.window

import kotlin.test.Test
import kotlin.test.assertEquals

class DefaultAttemptClaimViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newDefaultAttemptClaim("code-252", "title-922")
        val view = toDefaultAttemptClaimView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toDefaultAttemptClaimViews(mutableListOf<DefaultAttemptClaim>(item, item)).size, "views")
    }
}
