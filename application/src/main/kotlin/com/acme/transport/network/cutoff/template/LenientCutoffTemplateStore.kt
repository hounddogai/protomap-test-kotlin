package com.acme.transport.network.cutoff.template

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.network.cache.TtlCache
import com.acme.transport.network.cache.newTtlCache
import com.acme.transport.network.clock.Clock

/** Keeps lenient cutoff templates by code. */
interface LenientCutoffTemplateStore {
    fun find(code: String): LenientCutoffTemplate?
    fun save(item: LenientCutoffTemplate)
    fun remove(code: String): Boolean
    fun list(): MutableList<LenientCutoffTemplate>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryLenientCutoffTemplateStore(
    var items: MutableMap<String, LenientCutoffTemplate>,
    var order: MutableList<String>,
) : LenientCutoffTemplateStore {
    override fun find(code: String): LenientCutoffTemplate? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: LenientCutoffTemplate) {
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

    override fun list(): MutableList<LenientCutoffTemplate> {
        val out = mutableListOf<LenientCutoffTemplate>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryLenientCutoffTemplateStore(): LenientCutoffTemplateStore {
    return MemoryLenientCutoffTemplateStore(
        mutableMapOf<String, LenientCutoffTemplate>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedLenientCutoffTemplateStore(
    var inner: LenientCutoffTemplateStore,
    var cache: TtlCache<LenientCutoffTemplate>,
    var hits: Int,
    var misses: Int,
) : LenientCutoffTemplateStore {
    override fun find(code: String): LenientCutoffTemplate? {
        val hit = (cache.get(code) as LenientCutoffTemplate?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: LenientCutoffTemplate) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<LenientCutoffTemplate> = inner.list()
}

fun newCachedLenientCutoffTemplateStore(inner: LenientCutoffTemplateStore, ttl: Int, clock: Clock): LenientCutoffTemplateStore {
    return CachedLenientCutoffTemplateStore(
        inner = inner,
        cache = newTtlCache<LenientCutoffTemplate>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects lenient cutoff templates. Empty fields match all. */
data class LenientCutoffTemplateQuery(
    var channel: String,
    var minLoadKg: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: LenientCutoffTemplate): Boolean {
        if (channel != "" && normalize(item.channel) != normalize(channel)) {
            return false
        }
        if (item.loadKg < minLoadKg) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<LenientCutoffTemplate>): MutableList<LenientCutoffTemplate> {
        val matched = filter(items) { candidate: LenientCutoffTemplate -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findLenientCutoffTemplates(store: LenientCutoffTemplateStore, query: LenientCutoffTemplateQuery): MutableList<LenientCutoffTemplate> {
    return query.apply(store.list())
}
