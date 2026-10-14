package com.acme.transport.lastmile.drop.dropreservation

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.lastmile.cache.TtlCache
import com.acme.transport.lastmile.cache.newTtlCache
import com.acme.transport.lastmile.clock.Clock

/** Keeps drop rules by code. */
interface DropRuleStore {
    fun find(code: String): DropRule?
    fun save(item: DropRule)
    fun remove(code: String): Boolean
    fun list(): MutableList<DropRule>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryDropRuleStore(
    var items: MutableMap<String, DropRule>,
    var order: MutableList<String>,
) : DropRuleStore {
    override fun find(code: String): DropRule? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: DropRule) {
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

    override fun list(): MutableList<DropRule> {
        val out = mutableListOf<DropRule>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryDropRuleStore(): DropRuleStore {
    return MemoryDropRuleStore(mutableMapOf<String, DropRule>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedDropRuleStore(
    var inner: DropRuleStore,
    var cache: TtlCache<DropRule>,
    var hits: Int,
    var misses: Int,
) : DropRuleStore {
    override fun find(code: String): DropRule? {
        val hit = (cache.get(code) as DropRule?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: DropRule) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<DropRule> = inner.list()
}

fun newCachedDropRuleStore(inner: DropRuleStore, ttl: Int, clock: Clock): DropRuleStore {
    return CachedDropRuleStore(
        inner = inner,
        cache = newTtlCache<DropRule>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects drop rules. Empty fields match all. */
data class DropRuleQuery(
    var title: String,
    var minExpiresAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: DropRule): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
            return false
        }
        if (item.expiresAt < minExpiresAt) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<DropRule>): MutableList<DropRule> {
        val matched = filter(items) { candidate: DropRule -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findDropRules(store: DropRuleStore, query: DropRuleQuery): MutableList<DropRule> = query.apply(store.list())
