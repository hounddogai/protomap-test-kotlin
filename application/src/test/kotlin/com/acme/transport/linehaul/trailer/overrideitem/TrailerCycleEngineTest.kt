package com.acme.transport.linehaul.trailer.overrideitem

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class TrailerCycleEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultTrailerCycleEngine(newRegistry())
        val item = newTrailerCycle("code-99", "title-364")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
