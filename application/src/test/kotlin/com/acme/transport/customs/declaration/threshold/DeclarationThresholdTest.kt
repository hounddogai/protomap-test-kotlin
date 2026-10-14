package com.acme.transport.customs.declaration.threshold

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class DeclarationThresholdTest {
    @Test
    fun `starts in new`() {
        val item = newDeclarationThreshold("code-526", "title-740")
        assertEquals(DeclarationThresholdStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to queued`() {
        val item = newDeclarationThreshold("code-526", "title-740")
        try {
            item.moveTo(DeclarationThresholdStatus.QUEUED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(DeclarationThresholdStatus.QUEUED, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newDeclarationThreshold("code-526", "title-740")
        item.status = DeclarationThresholdStatus.FAILED
        assertFails("move") { item.moveTo(DeclarationThresholdStatus.NEW) }
    }
}
