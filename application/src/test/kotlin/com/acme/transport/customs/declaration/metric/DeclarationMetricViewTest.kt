package com.acme.transport.customs.declaration.metric

import kotlin.test.Test
import kotlin.test.assertEquals

class DeclarationMetricViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newDeclarationMetric("code-631", "title-764")
        val view = toDeclarationMetricView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toDeclarationMetricViews(mutableListOf<DeclarationMetric>(item, item)).size, "views")
    }
}
