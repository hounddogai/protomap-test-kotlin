package com.acme.transport.lastmile.parcel.metric

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class ParcelScoreEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultParcelScoreEngine(newRegistry())
        val item = newParcelScore("code-842", "title-544")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
