package com.acme.transport.network.cutoff.regionalcutoffsegment

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.network.cache.TtlCache
import com.acme.transport.network.cache.newTtlCache
import com.acme.transport.network.clock.Clock

/** Keeps cutoff templates by code. */
interface CutoffTemplateStore {
    fun find(code: String): CutoffTemplate?
    fun save(item: CutoffTemplate)
    fun remove(code: String): Boolean
    fun list(): MutableList<CutoffTemplate>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryCutoffTemplateStore(
    var items: MutableMap<String, CutoffTemplate>,
    var order: MutableList<String>,
) : CutoffTemplateStore {
    override fun find(code: String): CutoffTemplate? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: CutoffTemplate) {
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

    override fun list(): MutableList<CutoffTemplate> {
        val out = mutableListOf<CutoffTemplate>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryCutoffTemplateStore(): CutoffTemplateStore {
    return MemoryCutoffTemplateStore(mutableMapOf<String, CutoffTemplate>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedCutoffTemplateStore(
    var inner: CutoffTemplateStore,
    var cache: TtlCache<CutoffTemplate>,
    var hits: Int,
    var misses: Int,
) : CutoffTemplateStore {
    override fun find(code: String): CutoffTemplate? {
        val hit = (cache.get(code) as CutoffTemplate?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: CutoffTemplate) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<CutoffTemplate> = inner.list()
}

fun newCachedCutoffTemplateStore(inner: CutoffTemplateStore, ttl: Int, clock: Clock): CutoffTemplateStore {
    return CachedCutoffTemplateStore(
        inner = inner,
        cache = newTtlCache<CutoffTemplate>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects cutoff templates. Empty fields match all. */
data class CutoffTemplateQuery(
    var country: String,
    var minEndsAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: CutoffTemplate): Boolean {
        if (country != "" && normalize(item.country) != normalize(country)) {
            return false
        }
        if (item.endsAt < minEndsAt) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<CutoffTemplate>): MutableList<CutoffTemplate> {
        val matched = filter(items) { candidate: CutoffTemplate -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findCutoffTemplates(store: CutoffTemplateStore, query: CutoffTemplateQuery): MutableList<CutoffTemplate> {
    return query.apply(store.list())
}
