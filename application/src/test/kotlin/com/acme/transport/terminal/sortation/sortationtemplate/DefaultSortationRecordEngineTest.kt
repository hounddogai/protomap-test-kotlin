package com.acme.transport.terminal.sortation.sortationtemplate

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class DefaultSortationRecordEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultDefaultSortationRecordEngine(newRegistry())
        val item = newDefaultSortationRecord("code-341", "label-438")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
