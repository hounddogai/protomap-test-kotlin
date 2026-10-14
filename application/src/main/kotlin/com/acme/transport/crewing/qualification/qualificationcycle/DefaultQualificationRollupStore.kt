package com.acme.transport.crewing.qualification.qualificationcycle

import com.acme.transport.crewing.cache.TtlCache
import com.acme.transport.crewing.cache.newTtlCache
import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps default qualification rollups by code. */
interface DefaultQualificationRollupStore {
    fun find(code: String): DefaultQualificationRollup?
    fun save(item: DefaultQualificationRollup)
    fun remove(code: String): Boolean
    fun list(): MutableList<DefaultQualificationRollup>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryDefaultQualificationRollupStore(
    var items: MutableMap<String, DefaultQualificationRollup>,
    var order: MutableList<String>,
) : DefaultQualificationRollupStore {
    override fun find(code: String): DefaultQualificationRollup? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: DefaultQualificationRollup) {
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

    override fun list(): MutableList<DefaultQualificationRollup> {
        val out = mutableListOf<DefaultQualificationRollup>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryDefaultQualificationRollupStore(): DefaultQualificationRollupStore {
    return MemoryDefaultQualificationRollupStore(
        mutableMapOf<String, DefaultQualificationRollup>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedDefaultQualificationRollupStore(
    var inner: DefaultQualificationRollupStore,
    var cache: TtlCache<DefaultQualificationRollup>,
    var hits: Int,
    var misses: Int,
) : DefaultQualificationRollupStore {
    override fun find(code: String): DefaultQualificationRollup? {
        val hit = (cache.get(code) as DefaultQualificationRollup?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: DefaultQualificationRollup) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<DefaultQualificationRollup> = inner.list()
}

fun newCachedDefaultQualificationRollupStore(inner: DefaultQualificationRollupStore, ttl: Int, clock: Clock): DefaultQualificationRollupStore {
    return CachedDefaultQualificationRollupStore(
        inner = inner,
        cache = newTtlCache<DefaultQualificationRollup>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects default qualification rollups. Empty fields match all. */
data class DefaultQualificationRollupQuery(
    var label: String,
    var minStopCount: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: DefaultQualificationRollup): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
            return false
        }
        if (item.stopCount < minStopCount) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<DefaultQualificationRollup>): MutableList<DefaultQualificationRollup> {
        val matched = filter(items) { candidate: DefaultQualificationRollup -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findDefaultQualificationRollups(store: DefaultQualificationRollupStore, query: DefaultQualificationRollupQuery): MutableList<DefaultQualificationRollup> {
    return query.apply(store.list())
}
