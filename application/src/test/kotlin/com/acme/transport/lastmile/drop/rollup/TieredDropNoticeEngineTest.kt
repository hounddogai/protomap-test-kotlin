package com.acme.transport.lastmile.drop.rollup

import kotlin.test.Test
import kotlin.test.assertTrue

class TieredDropNoticeEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultTieredDropNoticeEngine()
        val item = newTieredDropNotice("code-377", "title-804")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
