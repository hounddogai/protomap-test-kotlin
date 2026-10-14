package com.acme.transport.linehaul.trailer.batch

import kotlin.test.Test
import kotlin.test.assertTrue

class TrailerEntryEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultTrailerEntryEngine()
        val item = newTrailerEntry("code-266", "name-585")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
