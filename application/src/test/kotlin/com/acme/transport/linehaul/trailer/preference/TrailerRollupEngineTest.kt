package com.acme.transport.linehaul.trailer.preference

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class TrailerRollupEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultTrailerRollupEngine(newRegistry())
        val item = newTrailerRollup("code-286", "name-523")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
