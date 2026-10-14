package com.acme.transport.terminal.dwell.score

import kotlin.test.Test
import kotlin.test.assertTrue

class ManualDwellProfileEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultManualDwellProfileEngine()
        val item = newManualDwellProfile("code-437", "title-133")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
