package com.acme.transport.linehaul.trailer.segment

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class ManualTrailerOverrideEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultManualTrailerOverrideEngine(newRegistry())
        val item = newManualTrailerOverride("code-698", "title-992")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
