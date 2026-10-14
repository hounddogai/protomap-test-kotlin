package com.acme.transport.crewing.roster.rule

import com.acme.transport.crewing.cache.TtlCache
import com.acme.transport.crewing.cache.newTtlCache
import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps roster templates by code. */
interface RosterTemplateStore {
    fun find(code: String): RosterTemplate?
    fun save(item: RosterTemplate)
    fun remove(code: String): Boolean
    fun list(): MutableList<RosterTemplate>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryRosterTemplateStore(
    var items: MutableMap<String, RosterTemplate>,
    var order: MutableList<String>,
) : RosterTemplateStore {
    override fun find(code: String): RosterTemplate? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: RosterTemplate) {
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

    override fun list(): MutableList<RosterTemplate> {
        val out = mutableListOf<RosterTemplate>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryRosterTemplateStore(): RosterTemplateStore {
    return MemoryRosterTemplateStore(mutableMapOf<String, RosterTemplate>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedRosterTemplateStore(
    var inner: RosterTemplateStore,
    var cache: TtlCache<RosterTemplate>,
    var hits: Int,
    var misses: Int,
) : RosterTemplateStore {
    override fun find(code: String): RosterTemplate? {
        val hit = (cache.get(code) as RosterTemplate?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: RosterTemplate) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<RosterTemplate> = inner.list()
}

fun newCachedRosterTemplateStore(inner: RosterTemplateStore, ttl: Int, clock: Clock): RosterTemplateStore {
    return CachedRosterTemplateStore(
        inner = inner,
        cache = newTtlCache<RosterTemplate>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects roster templates. Empty fields match all. */
data class RosterTemplateQuery(
    var source: String,
    var minGraceDays: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: RosterTemplate): Boolean {
        if (source != "" && normalize(item.source) != normalize(source)) {
            return false
        }
        if (item.graceDays < minGraceDays) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<RosterTemplate>): MutableList<RosterTemplate> {
        val matched = filter(items) { candidate: RosterTemplate -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findRosterTemplates(store: RosterTemplateStore, query: RosterTemplateQuery): MutableList<RosterTemplate> {
    return query.apply(store.list())
}
