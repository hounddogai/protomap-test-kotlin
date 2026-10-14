package com.acme.transport.terminal.dwell.notice

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.terminal.cache.TtlCache
import com.acme.transport.terminal.cache.newTtlCache
import com.acme.transport.terminal.clock.Clock

/** Keeps lenient dwell templates by code. */
interface LenientDwellTemplateStore {
    fun find(code: String): LenientDwellTemplate?
    fun save(item: LenientDwellTemplate)
    fun remove(code: String): Boolean
    fun list(): MutableList<LenientDwellTemplate>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryLenientDwellTemplateStore(
    var items: MutableMap<String, LenientDwellTemplate>,
    var order: MutableList<String>,
) : LenientDwellTemplateStore {
    override fun find(code: String): LenientDwellTemplate? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: LenientDwellTemplate) {
        if (!items.containsKey(item.code)) {
            order.add(item.code)
        }
        items[item.code] = item
    }

    override fun remove(code: String): Boolean {
        if (!items.containsKey(code)) {
            return false
        }
        items.remove(code)
        val kept = mutableListOf<String>()
        for (existing in order) {
            if (existing != code) {
                kept.add(existing)
            }
        }
        order = kept
        return true
    }

    override fun list(): MutableList<LenientDwellTemplate> {
        val out = mutableListOf<LenientDwellTemplate>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryLenientDwellTemplateStore(): LenientDwellTemplateStore {
    return MemoryLenientDwellTemplateStore(
        mutableMapOf<String, LenientDwellTemplate>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedLenientDwellTemplateStore(
    var inner: LenientDwellTemplateStore,
    var cache: TtlCache<LenientDwellTemplate>,
    var hits: Int,
    var misses: Int,
) : LenientDwellTemplateStore {
    override fun find(code: String): LenientDwellTemplate? {
        val hit = (cache.get(code) as LenientDwellTemplate?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: LenientDwellTemplate) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<LenientDwellTemplate> = inner.list()
}

fun newCachedLenientDwellTemplateStore(inner: LenientDwellTemplateStore, ttl: Int, clock: Clock): LenientDwellTemplateStore {
    return CachedLenientDwellTemplateStore(
        inner = inner,
        cache = newTtlCache<LenientDwellTemplate>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects lenient dwell templates. Empty fields match all. */
data class LenientDwellTemplateQuery(
    var unit: String,
    var minThreshold: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: LenientDwellTemplate): Boolean {
        if (unit != "" && normalize(item.unit) != normalize(unit)) {
            return false
        }
        if (item.threshold < minThreshold) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<LenientDwellTemplate>): MutableList<LenientDwellTemplate> {
        val matched = filter(items) { candidate: LenientDwellTemplate -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findLenientDwellTemplates(store: LenientDwellTemplateStore, query: LenientDwellTemplateQuery): MutableList<LenientDwellTemplate> {
    return query.apply(store.list())
}
