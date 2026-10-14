package com.acme.transport.terminal.sortation.window

import kotlin.test.Test
import kotlin.test.assertEquals

class RollingSortationWindowViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newRollingSortationWindow("code-727", "name-241")
        val view = toRollingSortationWindowView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toRollingSortationWindowViews(mutableListOf<RollingSortationWindow>(item, item)).size, "views")
    }
}
