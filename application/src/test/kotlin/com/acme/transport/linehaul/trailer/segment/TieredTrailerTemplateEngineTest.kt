package com.acme.transport.linehaul.trailer.segment

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class TieredTrailerTemplateEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultTieredTrailerTemplateEngine(newRegistry())
        val item = newTieredTrailerTemplate("code-35", "label-144")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
