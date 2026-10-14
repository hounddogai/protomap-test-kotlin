package com.acme.transport.customs.declaration.budget

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class TieredDeclarationBaselineTest {
    @Test
    fun `starts in proposed`() {
        val item = newTieredDeclarationBaseline("code-627", "label-682")
        assertEquals(TieredDeclarationBaselineStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to approved`() {
        val item = newTieredDeclarationBaseline("code-627", "label-682")
        try {
            item.moveTo(TieredDeclarationBaselineStatus.APPROVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(TieredDeclarationBaselineStatus.APPROVED, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newTieredDeclarationBaseline("code-627", "label-682")
        item.status = TieredDeclarationBaselineStatus.WITHDRAWN
        assertFails("move") { item.moveTo(TieredDeclarationBaselineStatus.PROPOSED) }
    }
}
