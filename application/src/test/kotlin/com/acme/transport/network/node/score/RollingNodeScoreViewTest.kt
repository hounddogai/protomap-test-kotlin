package com.acme.transport.network.node.score

import kotlin.test.Test
import kotlin.test.assertEquals

class RollingNodeScoreViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newRollingNodeScore("code-167", "title-796")
        val view = toRollingNodeScoreView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toRollingNodeScoreViews(mutableListOf<RollingNodeScore>(item, item)).size, "views")
    }
}
