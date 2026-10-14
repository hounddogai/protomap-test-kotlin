package com.acme.transport.customs.declaration.declarationmetric

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class LenientDeclarationMetricTest {
    @Test
    fun `starts in open`() {
        val item = newLenientDeclarationMetric("code-486", "title-793")
        assertEquals(LenientDeclarationMetricStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to archived`() {
        val item = newLenientDeclarationMetric("code-486", "title-793")
        try {
            item.moveTo(LenientDeclarationMetricStatus.ARCHIVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(LenientDeclarationMetricStatus.ARCHIVED, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newLenientDeclarationMetric("code-486", "title-793")
        item.status = LenientDeclarationMetricStatus.ARCHIVED
        assertFails("move") { item.moveTo(LenientDeclarationMetricStatus.OPEN) }
    }
}
