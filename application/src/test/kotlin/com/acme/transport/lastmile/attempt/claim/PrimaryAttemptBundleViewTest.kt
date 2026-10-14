package com.acme.transport.lastmile.attempt.claim

import kotlin.test.Test
import kotlin.test.assertEquals

class PrimaryAttemptBundleViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newPrimaryAttemptBundle("code-202", "title-258")
        val view = toPrimaryAttemptBundleView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toPrimaryAttemptBundleViews(mutableListOf<PrimaryAttemptBundle>(item, item)).size, "views")
    }
}
