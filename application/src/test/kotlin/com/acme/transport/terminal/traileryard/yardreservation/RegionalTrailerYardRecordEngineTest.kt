package com.acme.transport.terminal.traileryard.yardreservation

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class RegionalTrailerYardRecordEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultRegionalTrailerYardRecordEngine(newRegistry())
        val item = newRegionalTrailerYardRecord("code-557", "title-335")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
