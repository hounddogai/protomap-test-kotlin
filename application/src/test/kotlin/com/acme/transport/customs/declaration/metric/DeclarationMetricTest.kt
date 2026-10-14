package com.acme.transport.customs.declaration.metric

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class DeclarationMetricTest {
    @Test
    fun `starts in created`() {
        val item = newDeclarationMetric("code-631", "title-764")
        assertEquals(DeclarationMetricStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to retired`() {
        val item = newDeclarationMetric("code-631", "title-764")
        try {
            item.moveTo(DeclarationMetricStatus.RETIRED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(DeclarationMetricStatus.RETIRED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newDeclarationMetric("code-631", "title-764")
        item.status = DeclarationMetricStatus.RETIRED
        assertFails("move") { item.moveTo(DeclarationMetricStatus.CREATED) }
    }
}
