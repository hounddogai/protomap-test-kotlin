package com.acme.transport.terminal.sortation.rollup

import kotlin.test.Test
import kotlin.test.assertTrue

class GlobalSortationDraftEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultGlobalSortationDraftEngine()
        val item = newGlobalSortationDraft("code-191", "title-803")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
