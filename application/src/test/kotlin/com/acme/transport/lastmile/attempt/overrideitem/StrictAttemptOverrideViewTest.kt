package com.acme.transport.lastmile.attempt.overrideitem

import kotlin.test.Test
import kotlin.test.assertEquals

class StrictAttemptOverrideViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newStrictAttemptOverride("code-827", "label-466")
        val view = toStrictAttemptOverrideView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toStrictAttemptOverrideViews(mutableListOf<StrictAttemptOverride>(item, item)).size, "views")
    }
}
