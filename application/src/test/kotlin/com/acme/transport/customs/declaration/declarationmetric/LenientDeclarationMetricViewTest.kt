package com.acme.transport.customs.declaration.declarationmetric

import kotlin.test.Test
import kotlin.test.assertEquals

class LenientDeclarationMetricViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newLenientDeclarationMetric("code-486", "title-793")
        val view = toLenientDeclarationMetricView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toLenientDeclarationMetricViews(mutableListOf<LenientDeclarationMetric>(item, item)).size, "views")
    }
}
