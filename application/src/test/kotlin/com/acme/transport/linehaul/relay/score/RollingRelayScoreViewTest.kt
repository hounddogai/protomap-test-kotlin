package com.acme.transport.linehaul.relay.score

import kotlin.test.Test
import kotlin.test.assertEquals

class RollingRelayScoreViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newRollingRelayScore("code-272", "name-991")
        val view = toRollingRelayScoreView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toRollingRelayScoreViews(mutableListOf<RollingRelayScore>(item, item)).size, "views")
    }
}
