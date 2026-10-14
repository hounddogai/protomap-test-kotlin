package com.acme.transport.customs.declaration.declarationmetric

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class LegacyDeclarationSummaryTest {
    @Test
    fun `starts in created`() {
        val item = newLegacyDeclarationSummary("code-772", "name-469")
        assertEquals(LegacyDeclarationSummaryStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to validated`() {
        val item = newLegacyDeclarationSummary("code-772", "name-469")
        try {
            item.moveTo(LegacyDeclarationSummaryStatus.VALIDATED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(LegacyDeclarationSummaryStatus.VALIDATED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newLegacyDeclarationSummary("code-772", "name-469")
        item.status = LegacyDeclarationSummaryStatus.RETIRED
        assertFails("move") { item.moveTo(LegacyDeclarationSummaryStatus.CREATED) }
    }
}
